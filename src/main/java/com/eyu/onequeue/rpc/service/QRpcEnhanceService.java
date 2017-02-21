package com.eyu.onequeue.rpc.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QEnhanceException;
import com.eyu.onequeue.protocol.model.QRpc;
import com.eyu.onequeue.proxy.JavassistHepler;
import com.eyu.onequeue.proxy.model.ClassMetadata;
import com.eyu.onequeue.proxy.service.AbstractEnhanceService;
import com.eyu.onequeue.rpc.anno.QCommond;
import com.eyu.onequeue.rpc.anno.QModel;
import com.eyu.onequeue.rpc.model.IRpcContext;
import com.eyu.onequeue.rpc.model.RpcClassMetadata;
import com.eyu.onequeue.rpc.model.RpcContext;
import com.eyu.onequeue.rpc.model.RpcParam;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.ReflectUtil;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class QRpcEnhanceService extends AbstractEnhanceService {
    private static QRpcEnhanceService factory;
    private static Map<Short, RpcClassMetadata> classMetadatas = new HashMap<>();

    static {
	factory = new QRpcEnhanceService();
    }

    public static QRpcEnhanceService getFactory() {
	while (factory == null) {
	    Thread.interrupted();
	}
	return factory;
    }

    @Override
    public void initMetadata(Class<?> clz) {
	final short key = clz.getAnnotation(QModel.class).value();
	RpcClassMetadata classMetadata = classMetadatas.get(key);
	if (classMetadata != null) {
	    return;
	}
	synchronized (clz) {
	    classMetadata = classMetadatas.get(key);
	    if (classMetadata != null) {
		return;
	    }
	    classMetadata = RpcClassMetadata.of(clz);
	    classMetadatas.put(key, classMetadata);
	}
    }

    @Override
    public boolean matches(Method method) {
	return method.isAnnotationPresent(QCommond.class);
    }

    @Override
    public void addCtFields(Class<?> clz, CtClass enhancedClz) throws Exception {
	// enhancedClz.defrost();
	enhancedClz.addInterface(JavassistHepler.getCtClass(IRpcContext.class));

	CtField field = new CtField(JavassistHepler.getCtClass(RpcContext.class), "ctx", enhancedClz);
	field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
	// 添加JsonIgnore的注释到方法
	field.getFieldInfo2().addAttribute(JavassistHepler.addAnno(JsonIgnore.class, enhancedClz));
	enhancedClz.addField(field);
    }

    @Override
    public void addCtMethods(Class<?> clz, CtClass enhancedClz) throws Exception {
	final String ctxType = RpcContext.class.getName();
	CtMethod ctMethod = CtMethod.make("public " + ctxType + " getContext(){ return this.ctx; }", enhancedClz);
	enhancedClz.addMethod(ctMethod);

	ctMethod = CtMethod.make("public void setContext(" + ctxType + " ctx){ this.ctx=ctx; }", enhancedClz);
	enhancedClz.addMethod(ctMethod);

    }

    @Override
    public void buildEnhanceMethods(Class<?> clz, CtClass enhancedClz) {
	RpcClassMetadata classMetadata = (RpcClassMetadata) this.getClassMetadata(clz);
	final short key = clz.getAnnotation(QModel.class).value();

	classMetadata.getMethods().forEach((commondIndex, rpcMethod) -> {
	    Method method = rpcMethod.getMethod();
	    StringBuilder bodyBuilder = new StringBuilder();
	    String resultType = "";
	    if (void.class != method.getReturnType()) {
		resultType = " return ($r) ";
	    }
	    // 生成动态代码调用静态方法，减少书定复杂
	    bodyBuilder.append(resultType + QRpcEnhanceService.class.getName() + ".getFactory().proxy(this,$args,$sig, (short)" + key + "  ,(byte)" + commondIndex + "  );");
	    buildEnhanceMethod(clz, enhancedClz, method, bodyBuilder.toString());
	});

    }

    public Object proxy(IRpcContext target, Object[] args, Object[] types, short model, byte commondIndex) {
	final RpcClassMetadata classMetadata = (RpcClassMetadata) classMetadatas.get(model);

	final RpcContext ctx = target.getContext();

	try {
	    // 生成 QRpc
	    // indexs 格式 [0-下标,1-内容长度,N....]
	    List<Integer> indexList = new LinkedList<>();
	    List<byte[]> paramsList = new LinkedList<>();

	    int total = 0;
	    for (int i = 0; i < args.length; i++) {
		byte[] pBytes = classMetadata.encode(commondIndex, i, args[i]);
		if (pBytes == null || pBytes.length == 0) {
		    if (classMetadata.checkRequired(commondIndex, i)) {
			RpcParam rpcParam = classMetadata.getAndTryRpcParam(commondIndex, i);
			throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_SEND, "参数不允许为null index : " + i + " type : " + rpcParam.getType());
		    }

		    continue;
		}
		indexList.add(i);
		indexList.add(pBytes.length);
		paramsList.add(pBytes);
		total += pBytes.length;
	    }

	    byte[] indexs = new byte[0];
	    byte[] params = new byte[total];

	    if (total > 0) {
		indexs = SerialUtil.writeValueAsBytes(indexList);
	    }

	    // params
	    int offset = 0;
	    for (byte[] b : paramsList) {
		PacketUtil.writeBytes(offset, b, params);
		offset += b.length;
	    }

	    QRpc rpc = QRpc.of(classMetadata.getModel().value(), commondIndex, indexs, params);

	    byte[] bytes = rpc.toBytes();
	    rpc = QRpc.toObject(bytes);
	    Object ret = invoke(rpc);
	    // TODO
	    // ctx.send(rpc);
	    return ret;
	} catch (Exception e) {
	    throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_SEND, null, e);
	}
    }

    public void registerInvokeService(Object target) {
	final RpcClassMetadata classMetadata = (RpcClassMetadata) getClassMetadata(target.getClass());
	classMetadata.setTarget(target);
    }

    public Object invoke(QRpc rpc) {
	final short key = rpc.getModel();
	final byte command = rpc.getCommand();

	final RpcClassMetadata classMetadata = (RpcClassMetadata) classMetadatas.get(key);
	if (classMetadata == null) {
	    throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_NOFIND_MODEL, "model:" + key);
	}
	if (classMetadata.getTarget() == null) {
	    throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_NOFIDN_SERVICE, "clz:" + classMetadata.getClz() + " command : " + command);
	}
	if (classMetadata.getMethods().get(command) == null) {
	    throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_NOFIND_MODEL, "clz:" + classMetadata.getClz() + " command : " + command);
	}

	final byte[] indexs = rpc.getIndexs();
	final byte[] params = rpc.getParams();

	List<Integer> indexList = null;
	if (indexs.length > 0) {
	    indexList = SerialUtil.readList(indexs, Integer.class);
	} else {
	    indexList = new ArrayList<>(0);
	}

	// 参数检测
	final int paramSize = classMetadata.getMethods().get(command).getParams().size();
	Object[] args = new Object[paramSize];
	for (int i = 0; i < indexList.size(); i += 2) {
	    int paramIndex = indexList.get(i);
	    args[paramIndex] = 1;
	}

	for (int i = 0; i < args.length; i++) {
	    if (args[i] == null && classMetadata.checkRequired(command, i)) {
		RpcParam rpcParam = classMetadata.getAndTryRpcParam(command, i);
		throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_SEND, "clz:" + classMetadata.getClz() + " 参数不允许为null index : " + i + " type : " + rpcParam.getType());
	    }
	}
	for (int i = 0; i < args.length; i++) {
	    args[i] = null;
	}
	int offset = 0;
	for (int i = 0; i < indexList.size(); i += 2) {
	    int paramIndex = indexList.get(i);
	    int paramLen = indexList.get(i + 1);
	    byte[] bytes = PacketUtil.readBytes(offset, paramLen, params);
	    Object value = classMetadata.decode(command, paramIndex, bytes);
	    offset += paramLen;

	    args[paramIndex] = value;
	}

	return classMetadata.invoke(command, args);
    }

    @Override
    public ClassMetadata getClassMetadata(Class<?> clz) {
	QModel anno = ReflectUtil.getAnno(clz, QModel.class);
	final short key = anno.value();
	return classMetadatas.get(key);
    }
}

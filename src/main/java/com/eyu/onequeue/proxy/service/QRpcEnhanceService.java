package com.eyu.onequeue.proxy.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.eyu.onequeue.proxy.JavassistHepler;
import com.eyu.onequeue.proxy.model.rpc.IRpcContext;
import com.eyu.onequeue.proxy.model.rpc.RpcClassMetadata;
import com.eyu.onequeue.proxy.model.rpc.RpcContext;
import com.eyu.onequeue.rpc.anno.QCommond;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class QRpcEnhanceService extends AbstractEnhanceService {
    private static QRpcEnhanceService factory;

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
    public void initMetadata() {
	this.classMetadata = new RpcClassMetadata();
    }

    @Override
    public boolean matches(Method method) {
	return method.isAnnotationPresent(QCommond.class);
    }

    @Override
    public void addCtFields(Class<?> clz, CtClass enhancedClz) throws Exception {
	//enhancedClz.defrost();
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
	RpcClassMetadata classMetadata = (RpcClassMetadata) this.classMetadata;

	classMetadata.getMethods().forEach((commondIndex, rpcMethod) -> {
	    Method method = rpcMethod.getMethod();
	    StringBuilder bodyBuilder = new StringBuilder();
	    String resultType = "";
	    if (void.class != method.getReturnType()) {
		resultType = " return ($r) ";
	    }
	    //生成动态代码调用静态方法，减少书定复杂
	    bodyBuilder.append(resultType + QRpcEnhanceService.class.getName() + ".getFactory().proxy(this,$args,$sig, \"" + clz.getName() + "\" ," + commondIndex + "  );");
	    buildEnhanceMethod(clz, enhancedClz, method, bodyBuilder.toString());
	});

    }

    public Object proxy(IRpcContext ctx, Object[] args, Object[] types, String clzName, int commondIndex) {
	SerialUtil.println(args);
	return null;
    }
}

package com.eyu.onequeue.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QJsonException;
import com.eyu.onequeue.protocol.model.QSubscribe;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author solq
 **/
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class SerialUtil {
    /** 解压任务线程池 */
    private static ExecutorService executorService = PoolUtil.createCachePool("json array");
    public final static ObjectMapper MAPPER_CONVERT = new ObjectMapper();
    private final static int DEFUALT_BUFFER_SIZE = 1024;
    private final static int ZIP_LEVEL = Deflater.DEFAULT_COMPRESSION;
    public final static TypeReference<Collection<QSubscribe>> subType = new TypeReference<Collection<QSubscribe>>() {
    };

    public static <T> T formatMessage(Object b, TypeReference<T> tr) {
	return SerialUtil.map2Object((Map) b, tr);
    }

    public static byte[] zip(byte[] src) {
	Future<byte[]> future = executorService.submit(new Callable<byte[]>() {

	    @Override
	    public byte[] call() throws Exception {

		Deflater df = new Deflater(ZIP_LEVEL);
		df.setInput(src);
		df.finish();

		ByteArrayOutputStream baos = new ByteArrayOutputStream(DEFUALT_BUFFER_SIZE);
		byte[] buff = new byte[DEFUALT_BUFFER_SIZE];
		while (!df.finished()) {
		    int count = df.deflate(buff);
		    baos.write(buff, 0, count);
		}
		df.end();

		try {
		    baos.close();
		} catch (IOException e) {
		    // 永远不会执行的
		}
		return baos.toByteArray();
	    }
	});

	try {
	    return future.get();
	} catch (InterruptedException | ExecutionException e) {
	    FormattingTuple message = MessageFormatter.format("Zip : {}  ", src.length);
	    throw new QJsonException(QCode.ZIP_ERROR, message.getMessage(), e);
	}
    }

    public static byte[] unZip(byte[] src) {

	Future<byte[]> future = executorService.submit(new Callable<byte[]>() {
	    @Override
	    public byte[] call() throws Exception {
		Inflater inflater = new Inflater();
		inflater.setInput(src);
		int len = DEFUALT_BUFFER_SIZE;
		ByteArrayOutputStream os = new ByteArrayOutputStream(len);
		byte[] buff = new byte[len];
		try {
		    while (!inflater.finished()) {
			int count = inflater.inflate(buff);
			os.write(buff, 0, count);
		    }
		    inflater.end();
		} catch (DataFormatException e) {
		    FormattingTuple message = MessageFormatter.format("解压时发生数据格式异常  unZip : {}  ", src.length);
		    throw new QJsonException(QCode.UNZIP_ERROR, message.getMessage(), e);
		} finally {
		    try {
			os.close();
		    } catch (IOException e) {
			// 永远不会执行的
		    }
		}
		return os.toByteArray();
	    }
	});

	try {
	    return future.get(40, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
	    FormattingTuple message = MessageFormatter.format("解压时被打断  unZip : {}  ", src.length);
	    throw new QJsonException(QCode.UNZIP_ERROR, message.getMessage(), e);
	} catch (ExecutionException e) {
	    FormattingTuple message = MessageFormatter.format("解压时发生错误  unZip : {}  ", src.length);
	    throw new QJsonException(QCode.UNZIP_ERROR, message.getMessage(), e);
	} catch (TimeoutException e) {
	    FormattingTuple message = MessageFormatter.format("解压处理超时 unZip : {}  ", src.length);
	    throw new QJsonException(QCode.UNZIP_ERROR, message.getMessage(), e);
	}

    }

    public static <T> T readValueAsFile(String fileName, Class<T> clz) {
	try {
	    FileUtil.createDirs(fileName);
	    return MAPPER_CONVERT.readValue(new File(fileName), clz);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readValueAsFile : {} ## {}", fileName, clz);
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T readValueAsFile(String fileName, TypeReference<T> valueTypeRef) {
	try {
	    FileUtil.createDirs(fileName);
	    return MAPPER_CONVERT.readValue(new File(fileName), valueTypeRef);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readValueAsFile : {} ## {}", fileName, valueTypeRef.getType());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static void writeValueAsFile(String fileName, Object value) {
	try {
	    FileUtil.createDirs(fileName);
	    MAPPER_CONVERT.writeValue(new File(fileName), value);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("writeValueAsFile : {} ## {}", fileName, value.getClass());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> List<T> readList(byte[] src, Class<T> clz) {
	if (src == null) {
	    return null;
	}
	try {
	    Future<List<T>> future = executorService.submit(new Callable<List<T>>() {
		@Override
		public List<T> call() throws Exception {
		    CollectionLikeType type = TypeFactory.defaultInstance().constructCollectionType(LinkedList.class, clz);
		    return MAPPER_CONVERT.readValue(src, type);
		}
	    });
	    return future.get();
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readList :  ## {}", clz.getClass());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T[] readArray(byte[] src, Class<T> clz) {
	if (src == null || src.length == 0) {
	    return null;
	}
	try {
	    JavaType type = TypeFactory.defaultInstance().constructArrayType(clz);
	    return (T[]) MAPPER_CONVERT.readValue(src, type);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readList :  ## {}", clz.getClass());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T[] readArray(String src, Type type) {
	if (src == null) {
	    return null;
	}
	try {
	    JavaType javaType = TypeFactory.defaultInstance().constructArrayType(TypeFactory.defaultInstance().constructType(type));
	    return (T[]) MAPPER_CONVERT.readValue(src, javaType);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readArray : {} ## {}", src, type);
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T map2Object(Map mapData, TypeReference<T> tr) {
	if (mapData == null) {
	    return null;
	}
	try {
	    if (TypeUtils.isInstance(mapData, tr.getType())) {
		return (T) mapData;
	    }
	    return MAPPER_CONVERT.convertValue(mapData, tr);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("map2Object : {} ## {}", mapData.toString(), tr.getType());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T readValue(byte[] src, Class<T> type) {
	if (src == null) {
	    return null;
	}
	try {
	    return MAPPER_CONVERT.readValue(src, type);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readValue :  ## {}", type);
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static Object readValue(byte[] src, Type type) {
	if (src == null) {
	    return null;
	}
	try {
	    return MAPPER_CONVERT.readValue(src, TypeFactory.defaultInstance().constructType(type));
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readValue :  ## {}", type);
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T readValue(String src, Type type) {
	if (src == null) {
	    return null;
	}
	try {
	    JavaType valueTypeRef = TypeFactory.defaultInstance().constructType(type);
	    return MAPPER_CONVERT.readValue(src, valueTypeRef);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readValue : {} ## {}", src, type);

	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T readValue(String src, TypeReference<T> tr) {
	if (src == null) {
	    return null;
	}
	try {

	    return MAPPER_CONVERT.readValue(src.getBytes(), tr);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readValue : {} ## {}", src, tr.getType());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T readValue(byte[] src, TypeReference<T> valueTypeRef) {
	if (src == null) {
	    return null;
	}
	try {
	    return MAPPER_CONVERT.readValue(src, valueTypeRef);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readValue : {} ## {}", new String(src), valueTypeRef.getType());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static <T> T readZipValue(byte[] src, Class<T> valueType) {
	if (src == null) {
	    return null;
	}
	try {
	    return MAPPER_CONVERT.readValue(unZip(src), valueType);
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.format("readZipValue :  {}", valueType.getClass());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static byte[] writeValueAsBytes(Object obj) {
	try {
	    Future<byte[]> future = executorService.submit(new Callable<byte[]>() {
		@Override
		public byte[] call() throws Exception {
		    return MAPPER_CONVERT.writeValueAsBytes(obj);
		}
	    });
	    return future.get();
	} catch (Exception e) {

	    FormattingTuple message = MessageFormatter.format("writeValueAsBytes :  {}", obj.getClass());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static byte[] writeValueAsZipBytes(Object obj) {
	return zip(writeValueAsBytes(obj));
    }

    public static String writeValueAsString(Object obj) {
	try {
	    return MAPPER_CONVERT.writeValueAsString(obj);
	} catch (JsonProcessingException e) {
	    FormattingTuple message = MessageFormatter.format("writeValueAsString :  {}", obj.getClass());
	    throw new QJsonException(QCode.JSON_ERROR_DECODE, message.getMessage(), e);
	}
    }

    public static void println(Object obj) {
	System.out.println(writeValueAsString(obj));
    }

}

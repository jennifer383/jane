package com.common.jane_ai.threadlocal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// 应用参数上下文
public class BaseParameterHolder {

    private static final ThreadLocal<Map<String, String>> ThreadLocalMap = new ThreadLocal<>();

    public static void setBaseParameter(String key,String value){
        Map<String, String> map = ThreadLocalMap.get();
        if(map == null){
            map=new HashMap<>(64);
        }
        map.put(key,value);
        ThreadLocalMap.set(map);
    }

    public static String getBaseParameter(String key){
        return Optional.ofNullable(ThreadLocalMap.get()).map(map -> map.get(key)).orElse( null);
    }

    public static void remove(String key){
        Map<String, String> map = ThreadLocalMap.get();
        if(map != null){
            map.remove(key);
        }
    }

    public static ThreadLocal<Map<String,String>> getThreadLocal(){
    	return ThreadLocalMap;
    }

    public static Map<String,String> getThreadLocalMap(){
        Map<String, String> map = ThreadLocalMap.get();
        if(map==null){
            map=new HashMap<>(64);
        }
        return map;
    }

    public static void  setThreadLocalMap(Map<String,String> map){
    	ThreadLocalMap.set(map);
    }

    public static void removeThreadLocalMap(){
        ThreadLocalMap.remove();
    }








}

package org.example.spring_boot_starter.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.example.core.model.ServiceMetaInfo;
import org.example.core.proxy.ServiceProxyFactory;
import org.example.spring_boot_starter.annotation.RpcReference;
import org.example.spring_boot_starter.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

@Slf4j
public class RpcConsumerBootstrap implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] declaredFields = beanClass.getDeclaredFields();
        for(Field field:declaredFields){
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if(rpcReference!=null){
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if(interfaceClass==void.class){
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);
                Object proxy = ServiceProxyFactory.getProxy(interfaceClass);
                try{
                    field.set(bean,proxy);
                    field.setAccessible(false);
                }catch (IllegalAccessException e){
                    throw new RuntimeException("Failed to inject service proxy into field",e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean,beanName);
    }
}

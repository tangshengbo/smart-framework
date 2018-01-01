package com.tangshengbo.proxy;

/**
 * Created by TangShengBo on 2018/1/1.
 */
public interface Proxy {

    /**
     * 执行代理链对象
     * @param proxyChain
     * @return
     * @throws Throwable
     */
    Object doProxy(ProxyChain proxyChain) throws Throwable;
}

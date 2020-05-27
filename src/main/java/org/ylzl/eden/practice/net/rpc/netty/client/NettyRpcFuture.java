package org.ylzl.eden.practice.net.rpc.netty.client;

import lombok.SneakyThrows;
import org.ylzl.eden.practice.net.rpc.RpcResponse;

/**
 * Netty RPC 异步调用结果
 *
 * @author gyl
 * @since 2.0.0
 */
public class NettyRpcFuture {

  private volatile boolean isSucceed = false;

  private RpcResponse rpcResponse;

  private final Object lock = new Object();

  @SneakyThrows
  public RpcResponse get(int timeout) {
    synchronized (lock) {
      while (!isSucceed) {
        lock.wait(timeout);
      }
      return rpcResponse;
    }
  }

  public void set(RpcResponse response) {
    if (isSucceed) {
      return;
    }
    synchronized (lock) {
      this.rpcResponse = response;
      this.isSucceed = true;
      lock.notify();
    }
  }
}

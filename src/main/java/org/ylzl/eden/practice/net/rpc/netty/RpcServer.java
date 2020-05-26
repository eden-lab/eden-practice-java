/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ylzl.eden.practice.net.rpc.netty;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO
 *
 * @author gyl
 * @since 2.0.0
 */
public class RpcServer {

  private final AtomicBoolean initialized = new AtomicBoolean(false);

  private final String name;

  private final String host;

  private final Integer port;

  private int bossThreads;

  private int workerThreads;

  private int boundToPort = -1;

  private EventLoopGroup bossEventLoopGroup;

  private EventLoopGroup workerEventLoopGroup;

  private ChannelOptions channelOptions = new ChannelOptions();

  private ChannelOptions childChannelOptions = new ChannelOptions();

  private final List<ChannelHandler> channelHandlers = Lists.newArrayList();

  private final List<ChannelFutureListener> channelFutureListeners = Lists.newArrayList();

  public RpcServer(String name, String host, int port) {
    this.name = name;
    this.host = host;
    this.port = port;
    this.bossThreads = Runtime.getRuntime().availableProcessors();
    this.workerThreads = Runtime.getRuntime().availableProcessors();
  }

  public ListenableFuture<Void> startup() {
    System.out.printf(
        "Starting Netty server `%s` with %d boss threads and %d worker threads",
        name, bossThreads, workerThreads);

    final SettableFuture<Void> result = SettableFuture.create(); // 锁住返回结果
    final ServerBootstrap bootstrap = checkState().createServerBootstrap();
    final Channel channel = bootstrap.bind(host, port).syncUninterruptibly().channel();

    new Thread(
            () -> {
              final InetSocketAddress boundTo = (InetSocketAddress) channel.localAddress();
              final String hostName = boundTo.getAddress().getHostName();

              boundToPort = boundTo.getPort();
              System.out.printf("Started Netty server `%s` @%s:%d", name, hostName, boundToPort);

              result.set(null);
              channel.closeFuture().syncUninterruptibly();
            },
            name)
        .start();

    return result;
  }

  public void shutdown() {
    System.out.println("Stopping Netty server " + name);

    workerEventLoopGroup.shutdownGracefully();
    bossEventLoopGroup.shutdownGracefully();

    workerEventLoopGroup.terminationFuture().syncUninterruptibly();
    bossEventLoopGroup.terminationFuture().syncUninterruptibly();
  }

  public void addChannelHandler(final ChannelHandler channelHandler) {
    checkState().channelHandlers.add(channelHandler);
  }

  public void addAllChannelHandlers(final List<ChannelHandler> channelHandlers) {
    checkState().channelHandlers.addAll(channelHandlers);
  }

  public void addChannelFutureListener(final ChannelFutureListener channelFutureListener) {
    checkState().channelFutureListeners.add(channelFutureListener);
  }

  public void addAllChannelFutureListeners(
      final List<ChannelFutureListener> channelFutureListeners) {
    checkState().channelFutureListeners.addAll(channelFutureListeners);
  }

  private ServerBootstrap createServerBootstrap() {
    bossEventLoopGroup = new NioEventLoopGroup(bossThreads);
    workerEventLoopGroup = new NioEventLoopGroup(workerThreads);

    final ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossEventLoopGroup, workerEventLoopGroup);

    setOptions(bootstrap);
    initialized.set(true);

    return initServerBootstrap(bootstrap);
  }

  private ServerBootstrap initServerBootstrap(final ServerBootstrap bootstrap) {
    return bootstrap
        .channel(NioServerSocketChannel.class)
        .childHandler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(final SocketChannel socketChannel) {
                initChildChannel(socketChannel);
              }
            })
        .validate();
  }

  private void initChildChannel(final SocketChannel channel) {
    if (!channelHandlers.isEmpty()) {
      final ChannelPipeline pipeline = channel.pipeline();
      for (final ChannelHandler handler : channelHandlers) {
        pipeline.addLast(handler);
      }
    }

    if (!channelFutureListeners.isEmpty()) {
      for (final ChannelFutureListener listener : channelFutureListeners) {
        channel.closeFuture().addListener(listener);
      }
    }
  }

  private void setOptions(final ServerBootstrap bootstrap) {
    for (final Map.Entry<ChannelOption, Object> entry : channelOptions.get().entrySet()) {
      bootstrap.option(entry.getKey(), entry.getValue());
    }

    for (final Map.Entry<ChannelOption, Object> entry : childChannelOptions.get().entrySet()) {
      bootstrap.childOption(entry.getKey(), entry.getValue());
    }
  }

  private RpcServer checkState() {
    if (initialized.get()) {
      throw new IllegalStateException("Netty Server already initialized");
    }
    return this;
  }
}

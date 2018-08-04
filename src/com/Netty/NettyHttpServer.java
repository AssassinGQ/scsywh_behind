package com.Netty;

import com.Common.Utils.HibernateUtil;
import com.Netty.PipeLineHandler.HttpDiapathcer;
import com.Netty.PipeLineHandler.RequestDecoder;
import com.Netty.PipeLineHandler.WebSocketDiapachcer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyHttpServer {
	
	public void setup(int port) throws Exception
	{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast("respDecoder-reqEncoder", new HttpServerCodec());
						p.addLast("http-aggregator", new HttpObjectAggregator(2000000));
						p.addLast("http-chunked", new ChunkedWriteHandler());
//						p.addLast("user-handler", new RequestDiapathcer());
						p.addLast("user-requestdecoder", new RequestDecoder());
						p.addLast("user-httpdiapathcer", new HttpDiapathcer());
						p.addLast("user-wsdiapathcer", new WebSocketDiapachcer());
					}
				});
			ChannelFuture f = b.bind(port).sync();
			f.channel().closeFuture().sync();
		} finally {
			HibernateUtil.shutdown();
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}

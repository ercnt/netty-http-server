package ercnt.httpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpServer implements Runnable {

	public EventLoopGroup bossGroup, workerGroup;
	
	public ChannelFuture future;
	
	private int port;

	public HttpServer(int port) {
		this.port = port;
	}
	
	public void shutdown() {
        try {
        	
			this.bossGroup.shutdownGracefully().sync();
			this.bossGroup.shutdownGracefully().sync();
			this.future.channel().closeFuture().sync();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.bossGroup = new NioEventLoopGroup(1);
		this.workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			
			b.group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
				
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					
					p.addLast(new HttpRequestDecoder());
					p.addLast(new HttpResponseEncoder());
					p.addLast(new HttpServerHandler());
				}
			});

			try {
				
				this.future = b.bind(this.port).sync();
				this.future.channel().closeFuture().sync();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} finally {
			this.bossGroup.shutdownGracefully();
			this.workerGroup.shutdownGracefully();
		}
	}

}
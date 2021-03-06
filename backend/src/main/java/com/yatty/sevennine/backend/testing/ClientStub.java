package com.yatty.sevennine.backend.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatty.sevennine.api.dto.auth.LogInRequest;
import com.yatty.sevennine.util.PropertiesProvider;
import com.yatty.sevennine.util.codecs.JsonMessageDecoder;
import com.yatty.sevennine.util.codecs.JsonMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.UUID;

public class ClientStub {
    public static void main(String[] args) throws Exception {
        new ClientStub().run();
    }

    private void run() throws Exception {
        Properties p = PropertiesProvider.getEnvironmentProperties();
        EventLoopGroup elg = new NioEventLoopGroup();
        SocketAddress sa = new InetSocketAddress(
                p.getProperty(PropertiesProvider.Environment.HOST),
                Integer.valueOf(p.getProperty(PropertiesProvider.Environment.PORT))
        );

        Bootstrap b = new Bootstrap();
        b.group(elg)
                .channel(NioSocketChannel.class)
                .remoteAddress(sa)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new JsonMessageDecoder());
                        ch.pipeline().addLast(new EchoHandler());
                        ch.pipeline().addLast(new JsonMessageEncoder());
                    }
                });

        System.out.println("Client started");
        Channel c = b.connect().sync().channel();

        LogInRequest cr = new LogInRequest("Mike", UUID.randomUUID().toString());


        c.writeAndFlush(cr).addListener((e) -> {
            if (e.isSuccess()) {
                System.out.println("Message sent");
            } else {
                e.cause().printStackTrace();
            }
        });

        Thread.sleep(10000);
        elg.shutdownGracefully();
    }

    private static class PipeLineInitializer extends ChannelInitializer<NioDatagramChannel> {
        @Override
        protected void initChannel(NioDatagramChannel ch) throws Exception {
            ch.pipeline().addLast(new JsonMessageDecoder());
            ch.pipeline().addLast(new LogicHandler());
            ch.pipeline().addLast(new JsonMessageEncoder());
        }
    }

    private static class LogicHandler extends SimpleChannelInboundHandler<Object> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("Got data: " + msg.toString());
        }
    }

    public static class EchoHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            System.out.println("Got message");
        }
    }
}

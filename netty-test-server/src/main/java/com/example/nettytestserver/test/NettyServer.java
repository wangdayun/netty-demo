package com.example.nettytestserver.test;

import com.example.nettytestserver.init.NettyServerHandler;
import entity.Protobuf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * TODO
 *
 * @author: dayun_wang
 * @date: 2021-07-22 09:53
 **/
@Component
public class NettyServer {

    private Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    private static int port = 8098;

    /**
     * 启动netty服务器
     */
    @PostConstruct
    public void doStart() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {
                    ChannelPipeline channelPipeline = channel.pipeline();
                    channelPipeline.addLast(new IdleStateHandler(11, 0, 0));
                    channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
                    channelPipeline.addLast(new ProtobufDecoder(Protobuf.Message.getDefaultInstance()));
                    channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    channelPipeline.addLast(new ProtobufEncoder());
                    channelPipeline.addLast(new NettyServerHandler());
                }
            });
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            if (channelFuture.isSuccess()) {
                logger.info("服务器启动成功...");
            }
        } catch (InterruptedException inter) {
            inter.printStackTrace();
        }
    }
}

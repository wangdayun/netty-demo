package com.example.nettytestclient.test;

import com.example.nettytestclient.init.NettyClientHandler;
import entity.Protobuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author: dayun_wang
 * @date: 2021-07-22 10:27
 **/
@Component
public class NettyClient {

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static String address = "127.0.0.1";

    private static int port = 8098;

    @PostConstruct
    public static void doConnect(){
        NioEventLoopGroup workGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        try{
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    ChannelPipeline channelPipeline = channel.pipeline();
                    channelPipeline.addLast(new IdleStateHandler(0, 5, 0));
                    channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
                    channelPipeline.addLast(new ProtobufDecoder(Protobuf.Message.getDefaultInstance()));
                    channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    channelPipeline.addLast(new ProtobufEncoder());
                    channelPipeline.addLast(new NettyClientHandler());
                }
            });
            ChannelFuture channelFuture = bootstrap.connect(address, port).sync();
            channelFuture.channel().closeFuture().sync();
            if (!channelFuture.isSuccess()){
                logger.info("客户端正在重连！");
                channelFuture.channel().eventLoop().schedule(()-> doConnect(), 5, TimeUnit.SECONDS);
            }
        }catch (InterruptedException r){
            r.printStackTrace();
        }finally {
            workGroup.shutdownGracefully();
        }
    }
}

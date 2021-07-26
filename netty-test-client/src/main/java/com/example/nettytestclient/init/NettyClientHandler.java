package com.example.nettytestclient.init;

import com.example.nettytestclient.test.NettyClient;
import entity.Protobuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static constants.HandlerEnum.HEART;


/**
 * TODO
 *
 * @author: dayun_wang
 * @date: 2021-07-22 10:31
 **/
@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<Protobuf.Message> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * 重连机制
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        NettyClient.doConnect();
    }

    /**
     * 心跳机制-发送写
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt ;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE){
                Protobuf.Message message = Protobuf.Message.newBuilder()
                        .setType(HEART.getType()).build();
                ctx.writeAndFlush(message).addListeners((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        logger.error("IO error,close Channel");
                        future.channel().close();
                    }
                }) ;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端连接成功!");
    }

    /**
     * 业务处理
     *
     * @param channelHandlerContext
     * @param message
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protobuf.Message message) throws Exception {
        if (message.getType().equals(HEART.getType())){
            logger.info("收到服务器心跳...");
        }
    }
}

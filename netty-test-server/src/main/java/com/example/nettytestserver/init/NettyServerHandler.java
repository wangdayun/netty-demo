package com.example.nettytestserver.init;

import entity.Protobuf;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static constants.HandlerEnum.HEART;

/**
 * TODO
 *
 * @author: dayun_wang
 * @date: 2021-07-22 10:02
 **/
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Protobuf.Message> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private static final Map<String, Long> channelHashMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("通道连接成功：" + ctx.channel().id().asLongText());
        ctx.fireChannelActive();
    }

    /**
     * 心跳机制-超过11秒未进行读，则关闭客户端
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                ChannelId channelId = ctx.channel().id();
                long startTime = System.currentTimeMillis();
                Long endTime = channelHashMap.get(channelId.asLongText());
                if (endTime == null){
                    channelHashMap.put(channelId.asLongText(), startTime);
                    logger.info("定时检测客户端端是否存活");
                }else {
                    if ((startTime - endTime) > (11 * 1000)){
                        ctx.channel().close();
                    }
                }

                channelHashMap.put(channelId.asLongText(), startTime);
            }else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    /**
     * 具体业务处理
     *
     * @param ctx
     * @param message
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protobuf.Message message) throws Exception {
        if (message.getType().equals(HEART.getType())){
            logger.info("收到客户端心跳：" + message.toString());
            ChannelId channelId = ctx.channel().id();
            long startTime = System.currentTimeMillis();
            channelHashMap.put(channelId.asLongText(), startTime);
        }
    }
}

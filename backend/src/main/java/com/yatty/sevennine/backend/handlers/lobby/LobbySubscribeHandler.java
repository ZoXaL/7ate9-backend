package com.yatty.sevennine.backend.handlers.lobby;

import com.yatty.sevennine.api.dto.lobby.LobbyListSubscribeRequest;
import com.yatty.sevennine.api.dto.lobby.LobbyListUpdatedNotification;
import com.yatty.sevennine.backend.model.GameRegistry;
import com.yatty.sevennine.backend.model.LoginedUser;
import com.yatty.sevennine.backend.model.UserRegistry;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class LobbySubscribeHandler extends SimpleChannelInboundHandler<LobbyListSubscribeRequest> {
    private static final Logger logger = LoggerFactory.getLogger(LobbySubscribeHandler.class);
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LobbyListSubscribeRequest msg) throws Exception {
        LoginedUser user = UserRegistry.checkAndGetLoginedUser(msg.getAuthToken());
        logger.debug("Player '{}' is subscribing for updates", user.getUser().getGeneratedLogin());
    
        UserRegistry.addSubscriber(msg.getAuthToken());
        user.setChannel(ctx.channel());
        
        LobbyListUpdatedNotification lobbyListUpdatedNotification = new LobbyListUpdatedNotification();
        lobbyListUpdatedNotification.setLobbyList(GameRegistry.getLobbyListPublicInfo());
        
        ctx.channel().writeAndFlush(lobbyListUpdatedNotification);
    }
}
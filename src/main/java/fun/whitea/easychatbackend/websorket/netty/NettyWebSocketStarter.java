package fun.whitea.easychatbackend.websorket.netty;

import fun.whitea.easychatbackend.config.AppConfig;
import fun.whitea.easychatbackend.utils.StringTool;
import fun.whitea.easychatbackend.websorket.ChannelContextUtil;
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
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

@Component
public class NettyWebSocketStarter implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketStarter.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();

    @PreDestroy
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }


    @Resource
    HandlerWebSocket handlerWebSocket;
    @Resource
    HandleHeartBeat handleHeartBeat;
    @Resource
    AppConfig appConfig;

    @Override
    public void run() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 设置几个重要的处理器
                            // 对http的支持,使用http的编码器，解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            // 保证的接受的http请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            // 心跳
                            // readerIdleTime 读超时时间，测试段一定时间内未接受到被测试段消息
                            // writerIdleTime 写超时时间，测试段一定时间内向被测试段发送消息
                            // allIdleTime 所有类型操作时间
                            pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(handleHeartBeat);
                            // 将http协议升级为ws
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536, true, true, 10000L));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            Integer wsPort = appConfig.getWsPort();
            String wsPortStr = System.getProperty("ws.port");
            if (!StringTool.isEmpty(wsPortStr)) {
                wsPort = Integer.parseInt(wsPortStr);
            }
            ChannelFuture future = bootstrap.bind(wsPort).syncUninterruptibly();
            logger.info("Netty启动成功, 端口：{}", appConfig.getWsPort());
            future.channel().closeFuture().syncUninterruptibly();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

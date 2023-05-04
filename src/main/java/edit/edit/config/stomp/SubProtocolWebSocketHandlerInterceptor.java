package edit.edit.config.stomp;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class SubProtocolWebSocketHandlerInterceptor extends DelegatingIntroductionInterceptor {
    @Override
    protected Object doProceed(MethodInvocation mi) throws Throwable {
        if(mi.getMethod().getName().equals("afterConnectionEstablished") ) {
            WebSocketSession session = (WebSocketSession) mi.getArguments()[0];
            session.setTextMessageSizeLimit(50*1024*1024);
        }
        return super.doProceed(mi);
    }
}
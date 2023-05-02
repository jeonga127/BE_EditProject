package edit.edit.controller;

import edit.edit.dto.ResponseDto;
import edit.edit.dto.chat.ChatDto;
import edit.edit.dto.chat.ChatRoomRequestDto;
import edit.edit.jwt.JwtUtil;
import edit.edit.security.UserDetailsImpl;
import edit.edit.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final SimpMessagingTemplate msgOperation;
	private final JwtUtil jwtUtil;

	@PostMapping("/chat")
	public ResponseDto createChatRoom(@RequestBody ChatRoomRequestDto chatRoomRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
		return chatService.createChatRoom(chatRoomRequestDto, userDetails.getMember());
		// createChatRoom의 결과인 roomId와 type : ENTER을 저장한 chatDto에 넣어줘야함
	}

	@MessageMapping("/chat/enter")
	@SendTo("/sub/chat/room")
	public void enterChatRoom(ChatDto chatDto, SimpMessageHeaderAccessor headerAccessor) throws Exception {
		Thread.sleep(500); // simulated delay
		ChatDto newchatdto = chatService.enterChatRoom(chatDto, headerAccessor);
		msgOperation.convertAndSend("/sub/chat/room" + chatDto.getRoomId(), newchatdto);
	}

	@PostMapping("/chat/find")
	public ResponseDto findChatRoom(ChatRoomRequestDto chatRoomRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
		return chatService.findChatRoom(chatRoomRequestDto, userDetails.getMember());
	}

	@MessageMapping("/chat/send")
	@SendTo("/sub/chat/room")
	public void sendChatRoom(ChatDto chatDto) throws Exception {
		Thread.sleep(500); // simulated delay
		msgOperation.convertAndSend("/sub/chat/room" + chatDto.getRoomId(), chatDto);
	}

	@EventListener
	public void webSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		ChatDto chatDto = chatService.disconnectChatRoom(headerAccessor);
		msgOperation.convertAndSend("/sub/chat/room" + chatDto.getRoomId(), chatDto);
	}
}

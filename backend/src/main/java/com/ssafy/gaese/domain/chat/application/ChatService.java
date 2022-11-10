package com.ssafy.gaese.domain.chat.application;


import com.ssafy.gaese.domain.chat.dto.ChatDto;
import com.ssafy.gaese.domain.chat.dto.MessageDto;
import com.ssafy.gaese.domain.chat.repository.ChatRedisRepository;
import com.ssafy.gaese.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRedisRepository chatRedisRepository;

    public List<MessageDto> getAllMessages( String fromId, String toId){
        List<MessageDto> allMsg = new ArrayList<>();
        for( MessageDto messageDto : getMessages(fromId)){
            if(messageDto.getToUser().equals(toId)){
                allMsg.add(messageDto);
            }
        }

        for( MessageDto messageDto : getMessages(toId)){
            if(messageDto.getToUser().equals(toId)){
                allMsg.add(messageDto);
            }
        }
        return allMsg;
    }

    public List<MessageDto> getMessages(String fromId){
        List<MessageDto> res = chatRedisRepository.findAllById(Collections.singleton(fromId));
        res.addAll(chatRedisRepository.findAllById(Collections.singleton(fromId)));
        return res;
    }

    public void saveMessage(MessageDto messageDto){
        chatRedisRepository.save(messageDto);
    }

}

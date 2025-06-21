package com.example.gagso.Documents.helper;

import com.example.gagso.Documents.dto.DocumentDTO;
import com.example.gagso.WorkRoom.dto.TaskDTO;
import org.springframework.stereotype.Component;

@Component
public class DocumentValidator {
    /**
     * TaskDTO 유효성 검증
     * @param dto TaskDTO
     * @return 유효하지 않으면 에러 메시지, 유효하면 빈 문자열
     */
    public String validate(DocumentDTO dto) {
        if (dto == null) {
            return "요청 데이터가 없습니다.";
        }

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return "제목은 필수 항목입니다.";
        }

        if (dto.getContent() != null) {
            return "내용은 입력 필수 항목입니다.";

        }

//        if (dto.getDeptId() == null || dto.getDeptId().isBlank()) {
//            return "부서 ID는 필수입니다.";
//        }

        // 기타 유효성 검사 필요 시 여기에 추가

        return "";  // 문제가 없으면 빈 문자열 반환
    }
}

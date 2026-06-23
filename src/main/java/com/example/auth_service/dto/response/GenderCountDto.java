package com.example.auth_service.dto.response;


import com.example.auth_service.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class GenderCountDto {
    private Gender gender;
    private long count;
}
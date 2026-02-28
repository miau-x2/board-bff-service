package com.example.board.bff.api.auth.controller.dto.validation;

import jakarta.validation.GroupSequence;

@GroupSequence({NotBlankGroup.class, SizeGroup.class, FormatGroup.class})
public interface SignupValidationSequence {
}

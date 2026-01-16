package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.dto.MemberDTO;

public interface AuthenticationService {
    MemberDTO signInAndReturnJWT(MemberDTO signInRequest);
}

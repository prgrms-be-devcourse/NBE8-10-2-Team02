package com.back.test;

import com.back.global.igdb.TwitchTokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final TwitchTokenService twitchTokenService;

    public TestController(TwitchTokenService twitchTokenService) {
        this.twitchTokenService = twitchTokenService;
    }

    @GetMapping("/test/token")
    public String token() {
        return twitchTokenService.getAccessToken();
    }

}

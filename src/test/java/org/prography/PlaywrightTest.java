package org.prography;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaywrightTest {
    
    @Test
    @DisplayName(value = "동적 렌더링 후 네트워크 리소스 반환")
    void testDynamicRender() {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            )) {
                Page page = browser.newPage();

                // 네트워크 응답 기다림
                Response reviewResponse = page.waitForResponse(
                    response -> response.url().contains("/places/tab/reviews/kakaomap/17733090")
                        && response.status() == 200,
                    () -> page.navigate("https://place.map.kakao.com/17733090#comment")
                );

                // 응답 텍스트 출력 (JSON)
                String body = reviewResponse.text();
                System.out.println("🎯 응답 URL: " + reviewResponse.url());
                System.out.println("📄 응답 JSON:\n" + body);
            }
        }
    }
}
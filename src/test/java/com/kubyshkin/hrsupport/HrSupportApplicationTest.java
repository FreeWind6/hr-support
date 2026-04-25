package com.kubyshkin.hrsupport;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class HrSupportApplicationTest {

    @Test
    void main_delegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            HrSupportApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(
                    eq(HrSupportApplication.class),
                    any(String[].class)
            ));
        }
    }
}

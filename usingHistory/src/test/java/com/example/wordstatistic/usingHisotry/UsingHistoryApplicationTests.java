package com.example.wordstatistic.usingHisotry;

import com.example.wordstatistic.usingHisotry.repository.UsingHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsingHistoryApplicationTests {
    @MockBean
    final private UsingHistoryRepository usingHistoryRepository;

    @Autowired
    public UsingHistoryApplicationTests (final UsingHistoryRepository usingHistoryRepository) {
        this.usingHistoryRepository = usingHistoryRepository;
    }

	@Test
	void contextLoads() {
	}

}

package com.acornworks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.acornworks.projectset.ProjectsetApplication;

@ExtendWith(MockitoExtension.class)
public class ProjectsetApplicationTests {
    @Test
    public void testMain() {
        ProjectsetApplication.main(new String[] {});
    }
    
}

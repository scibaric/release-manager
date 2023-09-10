package com.tset.releasemanager

import org.junit.jupiter.api.BeforeEach

class ReleaseManagerServiceUnitTest {

    private lateinit var underTest: ReleaseManagerService

    @BeforeEach
    fun setUp() {
        underTest = ReleaseManagerServiceImpl()
    }


}
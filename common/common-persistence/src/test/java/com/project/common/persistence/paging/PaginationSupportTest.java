package com.project.common.persistence.paging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationSupportTest {

    @Test
    void clampsNegativePageToZero() {
        assertThat(PaginationSupport.safePage(-5)).isZero();
        assertThat(PaginationSupport.safePage(3)).isEqualTo(3);
    }

    @Test
    void capsRequestedPageSizeAtConfiguredMaximum() {
        assertThat(PaginationSupport.safeSize(1_000, 100)).isEqualTo(100);
        assertThat(PaginationSupport.safeSize(25, 100)).isEqualTo(25);
    }

    @Test
    void fallsBackToDefaultsForNonPositiveSize() {
        assertThat(PaginationSupport.safeSize(0, 100)).isEqualTo(PaginationSupport.DEFAULT_PAGE_SIZE);
        assertThat(PaginationSupport.safeSize(-1, 10)).isEqualTo(10);
    }

    @Test
    void computesOffset() {
        assertThat(PaginationSupport.offset(3, 20)).isEqualTo(60L);
        assertThat(PaginationSupport.offset(-1, 20)).isZero();
    }
}

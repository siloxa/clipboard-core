package tech.siloxa.clipboard.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tech.siloxa.clipboard.web.rest.TestUtil;

class ClipBoardTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClipBoard.class);
        ClipBoard clipBoard1 = new ClipBoard();
        clipBoard1.setId(1L);
        ClipBoard clipBoard2 = new ClipBoard();
        clipBoard2.setId(clipBoard1.getId());
        assertThat(clipBoard1).isEqualTo(clipBoard2);
        clipBoard2.setId(2L);
        assertThat(clipBoard1).isNotEqualTo(clipBoard2);
        clipBoard1.setId(null);
        assertThat(clipBoard1).isNotEqualTo(clipBoard2);
    }
}

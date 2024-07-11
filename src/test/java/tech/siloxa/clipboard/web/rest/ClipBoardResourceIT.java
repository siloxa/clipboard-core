package tech.siloxa.clipboard.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tech.siloxa.clipboard.IntegrationTest;
import tech.siloxa.clipboard.domain.ClipBoard;
import tech.siloxa.clipboard.repository.ClipBoardRepository;

/**
 * Integration tests for the {@link ClipBoardResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ClipBoardResourceIT {

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/clip-boards";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ClipBoardRepository clipBoardRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restClipBoardMockMvc;

    private ClipBoard clipBoard;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClipBoard createEntity(EntityManager em) {
        ClipBoard clipBoard = new ClipBoard().content(DEFAULT_CONTENT);
        return clipBoard;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClipBoard createUpdatedEntity(EntityManager em) {
        ClipBoard clipBoard = new ClipBoard().content(UPDATED_CONTENT);
        return clipBoard;
    }

    @BeforeEach
    public void initTest() {
        clipBoard = createEntity(em);
    }

    @Test
    @Transactional
    void createClipBoard() throws Exception {
        int databaseSizeBeforeCreate = clipBoardRepository.findAll().size();
        // Create the ClipBoard
        restClipBoardMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(clipBoard)))
            .andExpect(status().isCreated());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeCreate + 1);
        ClipBoard testClipBoard = clipBoardList.get(clipBoardList.size() - 1);
        assertThat(testClipBoard.getContent()).isEqualTo(DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    void createClipBoardWithExistingId() throws Exception {
        // Create the ClipBoard with an existing ID
        clipBoard.setId(1L);

        int databaseSizeBeforeCreate = clipBoardRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restClipBoardMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(clipBoard)))
            .andExpect(status().isBadRequest());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllClipBoards() throws Exception {
        // Initialize the database
        clipBoardRepository.saveAndFlush(clipBoard);

        // Get all the clipBoardList
        restClipBoardMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(clipBoard.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)));
    }

    @Test
    @Transactional
    void getClipBoard() throws Exception {
        // Initialize the database
        clipBoardRepository.saveAndFlush(clipBoard);

        // Get the clipBoard
        restClipBoardMockMvc
            .perform(get(ENTITY_API_URL_ID, clipBoard.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(clipBoard.getId().intValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT));
    }

    @Test
    @Transactional
    void getNonExistingClipBoard() throws Exception {
        // Get the clipBoard
        restClipBoardMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingClipBoard() throws Exception {
        // Initialize the database
        clipBoardRepository.saveAndFlush(clipBoard);

        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();

        // Update the clipBoard
        ClipBoard updatedClipBoard = clipBoardRepository.findById(clipBoard.getId()).get();
        // Disconnect from session so that the updates on updatedClipBoard are not directly saved in db
        em.detach(updatedClipBoard);
        updatedClipBoard.content(UPDATED_CONTENT);

        restClipBoardMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedClipBoard.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedClipBoard))
            )
            .andExpect(status().isOk());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
        ClipBoard testClipBoard = clipBoardList.get(clipBoardList.size() - 1);
        assertThat(testClipBoard.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void putNonExistingClipBoard() throws Exception {
        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();
        clipBoard.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClipBoardMockMvc
            .perform(
                put(ENTITY_API_URL_ID, clipBoard.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(clipBoard))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchClipBoard() throws Exception {
        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();
        clipBoard.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClipBoardMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(clipBoard))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamClipBoard() throws Exception {
        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();
        clipBoard.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClipBoardMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(clipBoard)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateClipBoardWithPatch() throws Exception {
        // Initialize the database
        clipBoardRepository.saveAndFlush(clipBoard);

        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();

        // Update the clipBoard using partial update
        ClipBoard partialUpdatedClipBoard = new ClipBoard();
        partialUpdatedClipBoard.setId(clipBoard.getId());

        partialUpdatedClipBoard.content(UPDATED_CONTENT);

        restClipBoardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClipBoard.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedClipBoard))
            )
            .andExpect(status().isOk());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
        ClipBoard testClipBoard = clipBoardList.get(clipBoardList.size() - 1);
        assertThat(testClipBoard.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void fullUpdateClipBoardWithPatch() throws Exception {
        // Initialize the database
        clipBoardRepository.saveAndFlush(clipBoard);

        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();

        // Update the clipBoard using partial update
        ClipBoard partialUpdatedClipBoard = new ClipBoard();
        partialUpdatedClipBoard.setId(clipBoard.getId());

        partialUpdatedClipBoard.content(UPDATED_CONTENT);

        restClipBoardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClipBoard.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedClipBoard))
            )
            .andExpect(status().isOk());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
        ClipBoard testClipBoard = clipBoardList.get(clipBoardList.size() - 1);
        assertThat(testClipBoard.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void patchNonExistingClipBoard() throws Exception {
        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();
        clipBoard.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClipBoardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, clipBoard.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(clipBoard))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchClipBoard() throws Exception {
        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();
        clipBoard.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClipBoardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(clipBoard))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamClipBoard() throws Exception {
        int databaseSizeBeforeUpdate = clipBoardRepository.findAll().size();
        clipBoard.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClipBoardMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(clipBoard))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClipBoard in the database
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteClipBoard() throws Exception {
        // Initialize the database
        clipBoardRepository.saveAndFlush(clipBoard);

        int databaseSizeBeforeDelete = clipBoardRepository.findAll().size();

        // Delete the clipBoard
        restClipBoardMockMvc
            .perform(delete(ENTITY_API_URL_ID, clipBoard.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ClipBoard> clipBoardList = clipBoardRepository.findAll();
        assertThat(clipBoardList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

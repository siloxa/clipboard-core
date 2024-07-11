package tech.siloxa.clipboard.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tech.siloxa.clipboard.IntegrationTest;
import tech.siloxa.clipboard.domain.WorkSpace;
import tech.siloxa.clipboard.repository.WorkSpaceRepository;

/**
 * Integration tests for the {@link WorkSpaceResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class WorkSpaceResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/work-spaces";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private WorkSpaceRepository workSpaceRepository;

    @Mock
    private WorkSpaceRepository workSpaceRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWorkSpaceMockMvc;

    private WorkSpace workSpace;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WorkSpace createEntity(EntityManager em) {
        WorkSpace workSpace = new WorkSpace().name(DEFAULT_NAME);
        return workSpace;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WorkSpace createUpdatedEntity(EntityManager em) {
        WorkSpace workSpace = new WorkSpace().name(UPDATED_NAME);
        return workSpace;
    }

    @BeforeEach
    public void initTest() {
        workSpace = createEntity(em);
    }

    @Test
    @Transactional
    void createWorkSpace() throws Exception {
        int databaseSizeBeforeCreate = workSpaceRepository.findAll().size();
        // Create the WorkSpace
        restWorkSpaceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(workSpace)))
            .andExpect(status().isCreated());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeCreate + 1);
        WorkSpace testWorkSpace = workSpaceList.get(workSpaceList.size() - 1);
        assertThat(testWorkSpace.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void createWorkSpaceWithExistingId() throws Exception {
        // Create the WorkSpace with an existing ID
        workSpace.setId(1L);

        int databaseSizeBeforeCreate = workSpaceRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWorkSpaceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(workSpace)))
            .andExpect(status().isBadRequest());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllWorkSpaces() throws Exception {
        // Initialize the database
        workSpaceRepository.saveAndFlush(workSpace);

        // Get all the workSpaceList
        restWorkSpaceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(workSpace.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllWorkSpacesWithEagerRelationshipsIsEnabled() throws Exception {
        when(workSpaceRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restWorkSpaceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(workSpaceRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllWorkSpacesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(workSpaceRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restWorkSpaceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(workSpaceRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getWorkSpace() throws Exception {
        // Initialize the database
        workSpaceRepository.saveAndFlush(workSpace);

        // Get the workSpace
        restWorkSpaceMockMvc
            .perform(get(ENTITY_API_URL_ID, workSpace.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(workSpace.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingWorkSpace() throws Exception {
        // Get the workSpace
        restWorkSpaceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWorkSpace() throws Exception {
        // Initialize the database
        workSpaceRepository.saveAndFlush(workSpace);

        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();

        // Update the workSpace
        WorkSpace updatedWorkSpace = workSpaceRepository.findById(workSpace.getId()).get();
        // Disconnect from session so that the updates on updatedWorkSpace are not directly saved in db
        em.detach(updatedWorkSpace);
        updatedWorkSpace.name(UPDATED_NAME);

        restWorkSpaceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedWorkSpace.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedWorkSpace))
            )
            .andExpect(status().isOk());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
        WorkSpace testWorkSpace = workSpaceList.get(workSpaceList.size() - 1);
        assertThat(testWorkSpace.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void putNonExistingWorkSpace() throws Exception {
        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();
        workSpace.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWorkSpaceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, workSpace.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(workSpace))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWorkSpace() throws Exception {
        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();
        workSpace.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkSpaceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(workSpace))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWorkSpace() throws Exception {
        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();
        workSpace.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkSpaceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(workSpace)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWorkSpaceWithPatch() throws Exception {
        // Initialize the database
        workSpaceRepository.saveAndFlush(workSpace);

        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();

        // Update the workSpace using partial update
        WorkSpace partialUpdatedWorkSpace = new WorkSpace();
        partialUpdatedWorkSpace.setId(workSpace.getId());

        restWorkSpaceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWorkSpace.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedWorkSpace))
            )
            .andExpect(status().isOk());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
        WorkSpace testWorkSpace = workSpaceList.get(workSpaceList.size() - 1);
        assertThat(testWorkSpace.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void fullUpdateWorkSpaceWithPatch() throws Exception {
        // Initialize the database
        workSpaceRepository.saveAndFlush(workSpace);

        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();

        // Update the workSpace using partial update
        WorkSpace partialUpdatedWorkSpace = new WorkSpace();
        partialUpdatedWorkSpace.setId(workSpace.getId());

        partialUpdatedWorkSpace.name(UPDATED_NAME);

        restWorkSpaceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWorkSpace.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedWorkSpace))
            )
            .andExpect(status().isOk());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
        WorkSpace testWorkSpace = workSpaceList.get(workSpaceList.size() - 1);
        assertThat(testWorkSpace.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingWorkSpace() throws Exception {
        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();
        workSpace.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWorkSpaceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, workSpace.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(workSpace))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWorkSpace() throws Exception {
        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();
        workSpace.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkSpaceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(workSpace))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWorkSpace() throws Exception {
        int databaseSizeBeforeUpdate = workSpaceRepository.findAll().size();
        workSpace.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkSpaceMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(workSpace))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the WorkSpace in the database
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWorkSpace() throws Exception {
        // Initialize the database
        workSpaceRepository.saveAndFlush(workSpace);

        int databaseSizeBeforeDelete = workSpaceRepository.findAll().size();

        // Delete the workSpace
        restWorkSpaceMockMvc
            .perform(delete(ENTITY_API_URL_ID, workSpace.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<WorkSpace> workSpaceList = workSpaceRepository.findAll();
        assertThat(workSpaceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

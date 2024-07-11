package tech.siloxa.clipboard.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import tech.siloxa.clipboard.domain.ClipBoard;
import tech.siloxa.clipboard.repository.ClipBoardRepository;
import tech.siloxa.clipboard.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link tech.siloxa.clipboard.domain.ClipBoard}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class ClipBoardResource {

    private final Logger log = LoggerFactory.getLogger(ClipBoardResource.class);

    private static final String ENTITY_NAME = "clipBoard";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ClipBoardRepository clipBoardRepository;

    public ClipBoardResource(ClipBoardRepository clipBoardRepository) {
        this.clipBoardRepository = clipBoardRepository;
    }

    /**
     * {@code POST  /clip-boards} : Create a new clipBoard.
     *
     * @param clipBoard the clipBoard to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new clipBoard, or with status {@code 400 (Bad Request)} if the clipBoard has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/clip-boards")
    public ResponseEntity<ClipBoard> createClipBoard(@RequestBody ClipBoard clipBoard) throws URISyntaxException {
        log.debug("REST request to save ClipBoard : {}", clipBoard);
        if (clipBoard.getId() != null) {
            throw new BadRequestAlertException("A new clipBoard cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ClipBoard result = clipBoardRepository.save(clipBoard);
        return ResponseEntity
            .created(new URI("/api/clip-boards/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /clip-boards/:id} : Updates an existing clipBoard.
     *
     * @param id the id of the clipBoard to save.
     * @param clipBoard the clipBoard to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated clipBoard,
     * or with status {@code 400 (Bad Request)} if the clipBoard is not valid,
     * or with status {@code 500 (Internal Server Error)} if the clipBoard couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/clip-boards/{id}")
    public ResponseEntity<ClipBoard> updateClipBoard(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ClipBoard clipBoard
    ) throws URISyntaxException {
        log.debug("REST request to update ClipBoard : {}, {}", id, clipBoard);
        if (clipBoard.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, clipBoard.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!clipBoardRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ClipBoard result = clipBoardRepository.save(clipBoard);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, clipBoard.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /clip-boards/:id} : Partial updates given fields of an existing clipBoard, field will ignore if it is null
     *
     * @param id the id of the clipBoard to save.
     * @param clipBoard the clipBoard to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated clipBoard,
     * or with status {@code 400 (Bad Request)} if the clipBoard is not valid,
     * or with status {@code 404 (Not Found)} if the clipBoard is not found,
     * or with status {@code 500 (Internal Server Error)} if the clipBoard couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/clip-boards/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ClipBoard> partialUpdateClipBoard(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ClipBoard clipBoard
    ) throws URISyntaxException {
        log.debug("REST request to partial update ClipBoard partially : {}, {}", id, clipBoard);
        if (clipBoard.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, clipBoard.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!clipBoardRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ClipBoard> result = clipBoardRepository
            .findById(clipBoard.getId())
            .map(existingClipBoard -> {
                if (clipBoard.getContent() != null) {
                    existingClipBoard.setContent(clipBoard.getContent());
                }

                return existingClipBoard;
            })
            .map(clipBoardRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, clipBoard.getId().toString())
        );
    }

    /**
     * {@code GET  /clip-boards} : get all the clipBoards.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of clipBoards in body.
     */
    @GetMapping("/clip-boards")
    public List<ClipBoard> getAllClipBoards() {
        log.debug("REST request to get all ClipBoards");
        return clipBoardRepository.findAll();
    }

    /**
     * {@code GET  /clip-boards/:id} : get the "id" clipBoard.
     *
     * @param id the id of the clipBoard to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the clipBoard, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/clip-boards/{id}")
    public ResponseEntity<ClipBoard> getClipBoard(@PathVariable Long id) {
        log.debug("REST request to get ClipBoard : {}", id);
        Optional<ClipBoard> clipBoard = clipBoardRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(clipBoard);
    }

    /**
     * {@code DELETE  /clip-boards/:id} : delete the "id" clipBoard.
     *
     * @param id the id of the clipBoard to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/clip-boards/{id}")
    public ResponseEntity<Void> deleteClipBoard(@PathVariable Long id) {
        log.debug("REST request to delete ClipBoard : {}", id);
        clipBoardRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

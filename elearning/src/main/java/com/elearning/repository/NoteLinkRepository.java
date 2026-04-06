package com.elearning.repository;

import com.elearning.model.entity.NoteLink;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteLinkRepository extends JpaRepository<NoteLink, Long> {

    @EntityGraph(attributePaths = {"toNote"})
    List<NoteLink> findByFromNoteId(Long fromNoteId);

    @EntityGraph(attributePaths = {"fromNote"})
    List<NoteLink> findByToNoteId(Long toNoteId);

    boolean existsByFromNoteIdAndToNoteId(Long fromId, Long toId);

    @Modifying
    @Query("DELETE FROM NoteLink l WHERE l.fromNote.id = :noteId OR l.toNote.id = :noteId")
    void deleteAllByNoteInvolved(@Param("noteId") Long noteId);
}

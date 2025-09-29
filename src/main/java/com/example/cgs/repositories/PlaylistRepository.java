package com.example.cgs.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cgs.entities.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist,Long> {

	List<Playlist> findAllByCourseid(Long id);

}

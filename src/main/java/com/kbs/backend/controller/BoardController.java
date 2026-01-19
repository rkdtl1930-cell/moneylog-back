package com.kbs.backend.controller;

import com.kbs.backend.dto.BoardDTO;
import com.kbs.backend.dto.FileDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.service.BoardService;
import net.coobird.thumbnailator.Thumbnailator;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    @Autowired
    private BoardService boardService;
    @Value("file.upload.path")
    private String uploadPath;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Long> createBoard(@RequestPart("board") BoardDTO boardDTO, @RequestPart List<MultipartFile> files) {
        if(files != null && !files.isEmpty()) {
            boardDTO.setFiles(uploadFiles(files));
        }
        Long id = boardService.registerBoard(boardDTO);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> getBoard(@PathVariable Long id) {
        BoardDTO boardDTO = boardService.findBoard(id);
        if(boardDTO == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(boardDTO);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Void> updateBoard(@PathVariable Long id,
                                            @RequestPart("board") BoardDTO boardDTO,
                                            @RequestPart(value="files", required=false)List<MultipartFile> files,
                                            @RequestPart(value="deleteFiles", required=false)List<String> deleteFiles) {
        boardDTO.setId(id);
        BoardDTO original = boardService.findBoard(id);
        List<FileDTO> originalFiles = original.getFiles();
        if(deleteFiles != null && !deleteFiles.isEmpty()) {
            List<FileDTO> deleteTargets = originalFiles.stream()
                    .filter(f -> deleteFiles.contains(f.getUuid()))
                    .toList();
            removeFiles(deleteTargets);
            originalFiles.removeAll(deleteTargets);
        }
        if(files != null && !files.isEmpty()) {
            originalFiles.addAll(uploadFiles(files));
        }
        boardDTO.setFiles(originalFiles);
        boardService.updateBoard(boardDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        BoardDTO boardDTO = boardService.findBoard(id);
        if(boardDTO !=null && boardDTO.getFiles() !=null){
            removeFiles(boardDTO.getFiles());
        }
        boardService.deleteBoard(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public PageResponseDTO<BoardDTO> getBoards(PageRequestDTO pageRequestDTO) {
        return boardService.getBoards(pageRequestDTO);
    }

    private List<FileDTO> uploadFiles(List<MultipartFile> files) {
        List<FileDTO> list = new ArrayList<>();
        String boardPath = Paths.get(uploadPath, "board").toString();

        files.forEach(file -> {
            String originalName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            Path savePath = Paths.get(boardPath, uuid + "_" + originalName);
            boolean image = false;

            try {
                file.transferTo(savePath);

                if (Files.probeContentType(savePath).startsWith("image")) {
                    image = true;
                    File thumbnail = new File(boardPath, "s_" + uuid + "_" + originalName);
                    Thumbnailator.createThumbnail(savePath.toFile(), thumbnail, 200, 200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            list.add(FileDTO.builder()
                    .uuid(uuid)
                    .fileName(originalName)
                    .image(image)
                    .build());
        });

        return list;
    }

    private void removeFiles(List<FileDTO> files) {
        String boardPath = Paths.get(uploadPath, "board").toString();

        for (FileDTO fileDTO : files) {
            String fileName = fileDTO.getUuid() + "_" + fileDTO.getFileName();
            Resource resource = new FileSystemResource(boardPath + File.separator + fileName);

            try {
                String contentType = Files.probeContentType(resource.getFile().toPath());
                resource.getFile().delete();

                if (contentType != null && contentType.startsWith("image")) {
                    File thumb = new File(boardPath + File.separator + "s_" + fileName);
                    thumb.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

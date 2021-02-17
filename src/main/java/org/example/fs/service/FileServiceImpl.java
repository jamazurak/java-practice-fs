package org.example.fs.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileServiceImpl implements FileService {

    @Override
    public byte[] read(final Path path) {
        try {
            return Files.readAllBytes(path);
        } catch(IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    @Override
    public Path write(final Path path, final String content) {
        try {
            return Files.write(path, content.getBytes());
        } catch(IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    @Override
    public boolean delete(final Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch(IOException x) {
            throw new UncheckedIOException(x);
        }
    }

}

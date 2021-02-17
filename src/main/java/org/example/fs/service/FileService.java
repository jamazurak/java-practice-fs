package org.example.fs.service;

import java.nio.file.Path;

public interface FileService {

    byte[] read(final Path path);

    Path write(final Path path, final String content);

    boolean delete(final Path path);

}

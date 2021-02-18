package com.example.fs.service;

import de.svenjacobs.loremipsum.LoremIpsum;
import lombok.extern.slf4j.Slf4j;
import org.example.fs.service.FileService;
import org.example.fs.service.FileServiceImpl;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FileServiceZipTest {

    public static final int BIG_AMOUNT = 1000000;

    private static final LoremIpsum LOREM_IPSUM = new LoremIpsum();

    private final Stopwatch
        stopwatchZipWrite = SimonManager.getStopwatch("ZipWrite"),
        stopwatchZipCopy = SimonManager.getStopwatch("ZipCopy");
    private Split split;

    @Test
    public void serviceImplTest() throws IOException {
        Path pathFs = new File("/tmp/zipfs").toPath();
        pathFs.toFile().mkdirs();

        FileService service = new FileServiceImpl();
        String lorem = LOREM_IPSUM.getParagraphs(BIG_AMOUNT);

        Map<String, Object> env = new HashMap<>();
        env.put("create", "true");
        FileSystemProvider provider = FileSystemProvider.installedProviders().stream()
            .filter(p -> "jar".equals(p.getScheme())).findFirst().orElse(null);
        if(provider == null) {
            throw new RuntimeException("Zip FS provider not found");
        }

        try(FileSystem fsZip = provider.newFileSystem(pathFs.resolve("fs.zip"), env)) {
            Path pathZip = fsZip.getPath("/");

            // Write
            for (int i = 0; i < 10; ++i) {
                Path fileZip = pathZip.resolve(String.format("lorem%ds.txt", i));
                split = stopwatchZipWrite.start();
                service.write(fileZip, lorem);
                split.stop();
                log.info("Write zip (size {}):\t{}", Files.size(fileZip), split);
            }

            // Copy
            for (int i = 0; i < 10; ++i) {
                Path fileZipSrc = pathZip.resolve(String.format("lorem%ds.txt", i)),
                    fileZipDst = pathZip.resolve(String.format("lorem%dd.txt", i));
                split = stopwatchZipCopy.start();
                Files.copy(fileZipSrc, fileZipDst, StandardCopyOption.REPLACE_EXISTING);
                service.delete(fileZipSrc);
                split.stop();
                log.info("Copy zip:\t{}", split);
            }

            for (String name: SimonManager.getSimonNames()) {
                log.info("Stopwatch \"{}\" stats:\t{}", name, SimonManager.getSimon(name));
            }

            try(DirectoryStream<Path> ds = Files.newDirectoryStream(pathZip)) {
                Map<String, Object> map;
                for(Path child: ds) {
                    map = Files.readAttributes(child, "zip:*");
                    log.info("{}\tsize {}\tcompressed size {}\tcrc {}",
                        child, map.get("size"), map.get("compressedSize"), map.get("crc"));
                }
            }
        }
    }

}

package com.example.fs.service;

import de.svenjacobs.loremipsum.LoremIpsum;
import lombok.extern.slf4j.Slf4j;
import org.cryptomator.cryptofs.CryptoFileSystemProperties;
import org.cryptomator.cryptofs.CryptoFileSystemProvider;
import org.example.fs.service.FileService;
import org.example.fs.service.FileServiceImpl;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.stream.Stream;

@Slf4j
public class FileServiceEncTest {

    public static final int BIG_AMOUNT = 1000000;

    private static final LoremIpsum LOREM_IPSUM = new LoremIpsum();

    private final Stopwatch
        stopwatchEncWrite = SimonManager.getStopwatch("EncWrite"),
        stopwatchEncCopy = SimonManager.getStopwatch("EncCopy");
    private Split split;

    @Test
    public void serviceImplTest() throws IOException {
        Path pathFs = new File("/tmp/cryptofs").toPath();
        pathFs.toFile().mkdirs();

        FileService service = new FileServiceImpl();
        String lorem = LOREM_IPSUM.getParagraphs(BIG_AMOUNT);

//        CryptoFileSystemProvider.initialize(pathFs, "masterkey.cryptomator", "password");

        try(FileSystem fsEnc = CryptoFileSystemProvider.newFileSystem(pathFs,
            CryptoFileSystemProperties.cryptoFileSystemProperties()
                .withPassphrase("password")
                .build())) {
            Path pathEnc = fsEnc.getPath("/");

            // Write
            for (int i = 0; i < 10; ++i) {
                Path fileEnc = pathEnc.resolve(String.format("lorem%ds.txt", i));
                split = stopwatchEncWrite.start();
                service.write(fileEnc, lorem);
                split.stop();
                log.info("Write enc (size {}):\t{}", Files.size(fileEnc), split);
            }

            // Copy
            for (int i = 0; i < 10; ++i) {
                Path fileEncSrc = pathEnc.resolve(String.format("lorem%ds.txt", i)),
                    fileEncDst = pathEnc.resolve(String.format("lorem%dd.txt", i));
                split = stopwatchEncCopy.start();
                Files.copy(fileEncSrc, fileEncDst, StandardCopyOption.REPLACE_EXISTING);
                service.delete(fileEncSrc);
                split.stop();
                log.info("Copy enc:\t{}", split);
            }

            for(String name: SimonManager.getSimonNames()) {
                log.info("Stopwatch \"{}\" stats:\t{}", name, SimonManager.getSimon(name));
            }

            try(Stream<Path> fileList = Files.list(pathEnc)) {
                fileList.forEach(file -> {
                    try {
                        BasicFileAttributeView attributeView =
                            Files.getFileAttributeView(file, BasicFileAttributeView.class);
                        log.info("{}\tsize {}", file, attributeView.readAttributes().size());
                    } catch(IOException x) {
                        log.error("Read attributes failed: {}", x.getMessage());
                    }
                });
            }
        }
    }

}

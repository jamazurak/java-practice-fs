package com.example.fs.service;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import de.svenjacobs.loremipsum.LoremIpsum;
import lombok.extern.slf4j.Slf4j;
import org.example.fs.service.FileService;
import org.example.fs.service.FileServiceImpl;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileServiceMemTest {

    public static final int BIG_AMOUNT = 1000000;

    private static final LoremIpsum LOREM_IPSUM = new LoremIpsum();

    private final Stopwatch
        stopwatchStdWrite = SimonManager.getStopwatch("StdWrite"),
        stopwatchStdCopy = SimonManager.getStopwatch("StdCopy"),
        stopwatchMemWrite = SimonManager.getStopwatch("MemWrite"),
        stopwatchMemCopy = SimonManager.getStopwatch("MemCopy");
    private Split split;

    @TempDir
    Path pathTemp;

    @Test
    public void serviceImplTest() throws IOException {
        FileService service = new FileServiceImpl();
        String lorem = LOREM_IPSUM.getParagraphs(BIG_AMOUNT);
        FileSystem fsMem = Jimfs.newFileSystem(
            Configuration.unix().toBuilder()
                .setMaxSize(8192 * 1048576L)
                .build()
        );
        Path pathMem = fsMem.getPath("");

        // Write
        for(int i = 0; i < 10; ++i) {
            Path fileStd = pathTemp.resolve(String.format("lorem%ds.txt", i));
            split = stopwatchStdWrite.start();
            service.write(fileStd, lorem);
            split.stop();
            log.info("Write standard (size {}):\t{}", Files.size(fileStd), split);

            Path fileMem = pathMem.resolve(String.format("lorem%ds.txt", i));
            split = stopwatchMemWrite.start();
            service.write(fileMem, lorem);
            split.stop();
            log.info("Write memory (size {}):\t{}", Files.size(fileMem), split);
        }

        // Copy
        for(int i = 0; i < 10; ++i) {
            Path fileStdSrc = pathTemp.resolve(String.format("lorem%ds.txt", i)),
                fileStdDst = pathTemp.resolve(String.format("lorem%dd.txt", i));
            split = stopwatchStdCopy.start();
            Files.copy(fileStdSrc, fileStdDst);
            service.delete(fileStdSrc);
            split.stop();
            log.info("Copy standard:\t{}", split);

            Path fileMemSrc = pathMem.resolve(String.format("lorem%ds.txt", i)),
                fileMemDst = pathMem.resolve(String.format("lorem%dd.txt", i));
            split = stopwatchMemCopy.start();
            Files.copy(fileMemSrc, fileMemDst);
            service.delete(fileMemSrc);
            split.stop();
            log.info("Copy memory:\t{}", split);
        }

        for(String name: SimonManager.getSimonNames()) {
            log.info("Stopwatch \"{}\" stats:\t{}", name, SimonManager.getSimon(name));
        }
    }

}

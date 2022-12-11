package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DaySeven {
    public interface FileSystemEntity {
        boolean isFile();
        boolean isDirectory();

        int getSize();
        FileSystemEntity setSize(int size);

        String getName();
        FileSystemEntity setName(String name);

        FileSystemEntity setParent(FileSystemEntity parent);
        FileSystemEntity getParent();

        ArrayList<FileSystemEntity> getChildren();
        FileSystemEntity addChild(FileSystemEntity entity);

        int getSizeOfChildren();
    }

    static abstract public class AbstractFileSystemEntity implements FileSystemEntity {
        protected String name;
        protected int size = 0;

        protected FileSystemEntity parent;

        @Override
        abstract public boolean isFile();
        @Override
        abstract public boolean isDirectory();

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public FileSystemEntity setSize(int size) {
            this.size = size;
            return this;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public FileSystemEntity setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public FileSystemEntity getParent() {
            return parent;
        }

        @Override
        public FileSystemEntity setParent(FileSystemEntity parent) {
            this.parent = parent;
            return this;
        }

        @Override
        abstract public FileSystemEntity addChild(FileSystemEntity entity);

        @Override
        abstract public int getSizeOfChildren();
    }

    public static class File extends AbstractFileSystemEntity {
        @Override
        public boolean isFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public ArrayList<FileSystemEntity> getChildren() {
            return new ArrayList<>();
        }

        @Override
        public FileSystemEntity addChild(FileSystemEntity entity) {
            throw new RuntimeException("A file can not have any children");
        }

        @Override
        public int getSizeOfChildren() {
            throw new RuntimeException("A file can not have any children");
        }
    }

    public static class Directory extends AbstractFileSystemEntity {
        protected ArrayList<FileSystemEntity> children = new ArrayList<>();

        @Override
        public boolean isFile() {
            return false;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public FileSystemEntity addChild(FileSystemEntity entity) {
            getChildren().add(entity);
            return this;
        }

        @Override
        public int getSizeOfChildren() {
            return getChildren().stream().mapToInt(entity -> {
                if (entity.isFile()) {
                    return entity.getSize();
                } else if (entity.isDirectory()) {
                    return entity.getSizeOfChildren();
                }
                return 0;
            }).sum();
        }

        public ArrayList<FileSystemEntity> getChildren() {
            return children;
        }
    }

    public static class DirectoryFilter {
        protected Directory directory;
        protected ArrayList<FileSystemEntity> filteredChildren;

        public DirectoryFilter(Directory directory) {
            this.directory = directory;
        }

        public DirectoryFilter filterDirectoriesBySize(int sizeRangeStart, int sizeRangeEnd) {
            if (filteredChildren == null) {
                filteredChildren = new ArrayList<>();
            }

            for (FileSystemEntity dir : directory.getChildren()) {
                if (dir instanceof Directory) {
                    if (dir.getSizeOfChildren() >= sizeRangeStart && dir.getSizeOfChildren() <= sizeRangeEnd) {
                        filteredChildren.add(dir);
                    }
                    (new DirectoryFilter((Directory) dir)).setFilterResult(getFilterResult())
                            .filterDirectoriesBySize(sizeRangeStart, sizeRangeEnd);
                }
            }

            return this;
        }

        protected DirectoryFilter setFilterResult(ArrayList<FileSystemEntity> filteredChildren) {
            this.filteredChildren = filteredChildren;
            return this;
        }
        public ArrayList<FileSystemEntity> getFilterResult() {
            return filteredChildren;
        }

    }

    public static class CommandLineInterface {
        Pattern changeDirectory = Pattern.compile("\\$\\scd\\s(.+)");
        Pattern fileRecord = Pattern.compile("(\\d+)\\s(.+)");
        Pattern directoryRecord = Pattern.compile("dir\\s(.+)");

        Pattern[] patterns = new Pattern[] {
                changeDirectory,
                fileRecord,
                directoryRecord
        };
        Directory workingDirectory;
        Directory root;

        public CommandLineInterface(Directory root, Directory workingDirectory, String command) {
            this.workingDirectory = workingDirectory;
            this.root = root;

            Matcher match = getMatchForPattern(command);

            if (match == null) {
                return;
            }

            Pattern pattern = match.pattern();

            if (pattern == changeDirectory) {
                handleDirectoryChange(match);
            } else if (pattern == fileRecord) {
                handleFileRecord(match);
            } else if (pattern == directoryRecord) {
                handleDirectoryRecord(match);
            } else {
                System.out.println("\"" + command + "\" unknown");
            }
        }

        public Directory getWorkingDirectory() {
            return workingDirectory;
        }

        public Directory getRoot() {
            return root;
        }

        public CommandLineInterface setWorkingDirectory(Directory newWorkingDirectory) {
            workingDirectory = newWorkingDirectory;
            return this;
        }

        protected void handleDirectoryChange(Matcher match) {
            String directoryName = match.group(1);

            if (directoryName.equals("/")) {
                setWorkingDirectory(getRoot());
            }

            if (directoryName.equals("..")) {
                setWorkingDirectory((Directory) getWorkingDirectory().getParent());
            }

            Optional<FileSystemEntity> foundDir = this.getWorkingDirectory().children.stream()
                    .filter(dir -> dir instanceof Directory && dir.getName().equals(directoryName))
                    .findFirst();

            if (foundDir.isPresent()) {
                setWorkingDirectory((Directory)foundDir.get());
            }
        }

        protected void handleFileRecord(Matcher match) {
            int fileSize = Integer.valueOf(match.group(1));
            String fileName = match.group(2);
            File file = (File) (new File()).setParent(getWorkingDirectory()).setName(fileName).setSize(fileSize);
            getWorkingDirectory().addChild(file);
        }

        protected void handleDirectoryRecord(Matcher match) {
            String directoryName = match.group(1);
            Directory dir = (Directory) (new Directory()).setName(directoryName).setParent(getWorkingDirectory());
            getWorkingDirectory().addChild(dir);
        }

        protected Matcher getMatchForPattern(String command) {
            for (Pattern pattern: patterns) {
                Matcher matcher = pattern.matcher(command);
                if (matcher.find()) {
                    return matcher;
                }
            }
            return null;
        }
    }

    public static void main(String[] args) {
        Directory root = (Directory) new Directory().setName("/");
        Directory currentDirectory = root;
        try (Stream<String> stringStream = Files.lines(Paths.get(args[0]))) {
            for (Iterator<String> it = stringStream.iterator(); it.hasNext(); ) {
                String command = it.next();
                currentDirectory = (new CommandLineInterface(root, currentDirectory, command)).getWorkingDirectory();
            }

            // Part 1
            ArrayList<FileSystemEntity> result = new DirectoryFilter(root)
                    .filterDirectoriesBySize(0, 100000)
                    .getFilterResult();

            int sum = result.stream().mapToInt(dir -> dir.getSizeOfChildren()).sum();
            System.out.println("Part 1: " + sum);

            // Part 2
            final int TOTAL_SPACE  = 70000000;
            final int TOTAL_UNUSED_SPACE_NEEDED = 30000000;
            int neededSpace = TOTAL_UNUSED_SPACE_NEEDED - (TOTAL_SPACE - root.getSizeOfChildren());
            OptionalInt dirToDeleteSize = new DirectoryFilter(root)
                    .filterDirectoriesBySize(neededSpace, Integer.MAX_VALUE)
                    .getFilterResult()
                    .stream()
                    .mapToInt(dir -> dir.getSizeOfChildren()).min();
            System.out.println("Part 2: " + dirToDeleteSize.getAsInt());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

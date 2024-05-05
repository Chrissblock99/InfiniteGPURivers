package me.chriss99;

import org.lwjgl.opengl.*;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.Scanner;


public class Main {

    //a variable to hold the id of the GLFW window
    static long window;

    static int vao;
    static int coordVBO;
    static int colorVBO;
    static int indicesVBO;

    static int vertexShader;
    static int fragmentShader;
    static int program;

    static int transformMatrix;
    static InputDeviceManager inputDeviceManager = null;
    static CameraMatrix cameraMatrix = new CameraMatrix();
    static MovementController movementController = null;

    public static void main(String[] args) {
        glfwInit();
        createWindow();
        setupData();
        setupProgram();
        inputDeviceManager = new InputDeviceManager(window);
        movementController = new MovementController(inputDeviceManager, cameraMatrix);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        loop();
        System.out.println("Window closed");

        cleanGL();
    }

    private static void createWindow() {
        final int screenWidth = glfwGetVideoMode(glfwGetPrimaryMonitor()).width()/2;
        final int screenHeight = glfwGetVideoMode(glfwGetPrimaryMonitor()).height()/2;
        cameraMatrix.aspectRatio = (float) screenHeight / (float) screenWidth;

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        //create a GLFW window and store its id in the window variable
        window = glfwCreateWindow(screenWidth, screenHeight, "GLFW OpenGL Window", NULL, 0);

        //enables opengl
        glfwMakeContextCurrent(window);

        //create GLCapabilities instance because it's required (stupid, I know) and use it to print out if OpenGL 4.5 is supported
        System.out.println("OpenGL 4.5 Supported: " + GL.createCapabilities().OpenGL45);

        //make the opengl screen 1600 pixels wide and 900 pixels tall.
        glViewport(0, 0, screenWidth, screenHeight);

        //show the window
        glfwShowWindow(window);
    }

    static int[] index;
    private static void setupData() {
        double[] triangle = {
                0, 0, 0,
                1, 0, 0,
                0, 1, 0,
                1, 1, 0,
                0, 1, 1,
                1, 1, 1,
                0, 0, 1,
                1, 0, 1,
        };

        //the color data (red, green, and blue)
        double[] color = {
                0.0, 1.0, 0.0,
                1.0, 0.0, 0.0,
                0.0, 0.0, 1.0,
                0.0, 1.0, 0.0,
                1.0, 0.0, 0.0,
                0.0, 0.0, 1.0,
                0.0, 1.0, 0.0,
                1.0, 0.0, 0.0,
        };

        //the order to render the vertices
        index = new int[] {
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7
        };

        //convert the vertex data arrays into ByteBuffers using a method I created down below
        ByteBuffer vertices = storeArrayInBuffer(triangle);
        ByteBuffer colors = storeArrayInBuffer(color);
        ByteBuffer indices = storeArrayInBuffer(index);

        //VAO: stores pointers to all of the vbos to keep 'em organized
        //VBO: stores data (vertex coordinates, colors, indices, etc.) and a header that contains information about their format

        //tell the GPU to make a single vertex array and store the returned id into the VBO int
        vao = glGenVertexArrays();

        //set the current vertex array object
        glBindVertexArray(vao);

        //tell the gpu to make a VBO and store its ID in the 'coordVBO' varabile
        coordVBO = glGenBuffers();

        //bind the 'coordVBO' VBO for use
        glBindBuffer(GL_ARRAY_BUFFER, coordVBO);

        //we are currently inside the vertex array so this VBO is associated with 'coordVBO'
        //uploads VBO data (in this case, coords) to the GPU, tells some information about the VBO so that it can work as efficiently as possible
        //we are using STATIC_DRAW because "The data store contents will be speciÃ¯Â¬Âed once by the application...
        //...and used many times as the source for GL drawing and image speciÃ¯Â¬Âcation commands."
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        //specifies information about the format of the VBO (number of values per vertex, data type, etc.)
        glVertexAttribPointer(0, 3, GL_DOUBLE, false, 0, 0);

        //enable vertex attribute array 0
        glEnableVertexAttribArray(0);

        //create a second VBO for the colors
        colorVBO = glGenBuffers();

        //bind the 'colorVBO' VBO for use
        glBindBuffer(GL_ARRAY_BUFFER, colorVBO);

        //uploads VBO data (in this case, colors) to the GPU
        glBufferData(GL_ARRAY_BUFFER, colors, GL_STATIC_DRAW);

        //specifies information about the format of the VBO (number of values per vertex, data type, etc.)
        glVertexAttribPointer(1, 3, GL_DOUBLE, false, 0, 0);

        //enable vertex attribute array 1
        glEnableVertexAttribArray(1);

        //create a third VBO for the indices (tells the GPU which vertices to render and when)
        indicesVBO = glGenBuffers();

        //bind the 'indicesVBO' for use
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);

        //uploads VBO data (in this case, colors) to the GPU
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        //unbind the last bound VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //unbind the currently bound VAO
        glBindVertexArray(0);
    }

    private static void setupProgram() {
        //load the vertex shader from the file using a method I wrote down below
        vertexShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/shader.vert"), GL_VERTEX_SHADER);

        //load the fragment shader from the file using a method I wrote down below
        fragmentShader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/shader.frag"), GL_FRAGMENT_SHADER);

        //create a program object and store its ID in the 'program' variable
        program = glCreateProgram();

        //these method calls link shader program variables to attribute locations so that they can be modified in Java code
        glBindAttribLocation(program, 0, "position");
        glBindAttribLocation(program, 1, "color");

        //attach the vertex and fragment shaders to the program
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        //link the program (whatever that does)
        glLinkProgram(program);

        //validate the program to make sure it wont blow up the program
        glValidateProgram(program);

        //check for compilation errors
        System.out.println("Vertex Shader Compiled: " 		+ glGetShaderi(vertexShader, 	GL_COMPILE_STATUS));
        System.out.println("Fragment Shader Compiled: " 	+ glGetShaderi(fragmentShader, 	GL_COMPILE_STATUS));
        System.out.println("Program Linked: " 				+ glGetProgrami(program, 		GL_LINK_STATUS));
        System.out.println("Program Validated: " 			+ glGetProgrami(program, 		GL_VALIDATE_STATUS));
        printErrors();

        //sets the background clear color to white
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //get the 'colorMod and 'positionMod' variables so I can change them while drawing to create the animation
        transformMatrix = glGetUniformLocation(program, "transformMatrix");

        //set the current program
        glUseProgram(program);
    }

    private static void loop() {
        while(!glfwWindowShouldClose(window)) {
            movementController.update();
            glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));

            //clear the window
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            //draw the current bound VAO/VBO using an index buffer
            glDrawElements(GL_TRIANGLE_STRIP, index.length, GL_UNSIGNED_INT, 0);

            //swap the frame to show the rendered image
            glfwSwapBuffers(window);

            //poll for window events (resize, close, button presses, etc.)
            glfwPollEvents();
        }
    }

    private static void cleanGL() {
        //disable the vertex attribute arrays
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        //delete the vbos and vao
        glDeleteBuffers(coordVBO);
        glDeleteBuffers(colorVBO);
        glDeleteBuffers(indicesVBO);
        glDeleteVertexArrays(vao);

        //detach the shaders from the program object
        glDetachShader(program, vertexShader);
        glDetachShader(program, fragmentShader);

        //delete the shaders now that they are deatched
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        //stop using the shader program
        glUseProgram(0);

        //delete the program now that the shaders are detached and the program isn't being used
        glDeleteProgram(program);

        printErrors();
    }

    private static void printErrors() {
        int error = glGetError();
        while(error != 0) {
            System.out.println("OpenGL Error: " + error);
            error = glGetError();
        }
    }

    public static int loadShader(File file, int type) {
        try {
            Scanner sc = new Scanner(file);
            StringBuilder data = new StringBuilder();

            if(file.exists()) {
                while(sc.hasNextLine()) {
                    data.append(sc.nextLine()).append("\n");
                }

                sc.close();
            }
            int id = glCreateShader(type);
            glShaderSource(id, data);
            glCompileShader(id);
            return id;
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static ByteBuffer storeArrayInBuffer(double[] array) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(array.length * 8);

        for(double i : array)
            buffer.putDouble(i);

        buffer.position(0);
        return buffer;
    }

    public static ByteBuffer storeArrayInBuffer(int[] array) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(array.length * 4);

        for(int i : array)
            buffer.putInt(i);

        buffer.position(0);
        return buffer;
    }
}
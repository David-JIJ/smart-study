package com.example.smart_study.controller;

import com.example.smart_study.entity.User;
import com.example.smart_study.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.web.bind.annotation.CrossOrigin; // 如果没自动导入，就加上这句
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin  // <--- 加上这一行！！！允许网页跨域访问
@RestController
public class AiTestController {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Resource
    private UserMapper userMapper;

    @Resource
    private com.example.smart_study.mapper.NoteMapper noteMapper;

    // --- 1. 登录验证接口 ---
    @GetMapping("/login")
    public String login(String username, String password) {
        User user = userMapper.login(username, password);
        if (user != null) {
            return "SUCCESS:" + user.getId() + ":" + user.getUsername() + ":" + user.getRole();
        } else {
            return "FAIL:用户名或密码错误";
        }
    }

    // --- 2. 智能欢迎接口 ---
    @GetMapping("/smartLogin")
    public String smartLogin(Integer id) {
        User user = userMapper.findById(id);
        if (user == null) return "用户不存在";

        try {
            String prompt = String.format("我是%s，身份是%s，请给我写一段50字以内的热情欢迎语。",
                    user.getUsername(), user.getRole());
            return callAi(prompt);
        } catch (Exception e) {
            return "欢迎你，" + user.getUsername();
        }
    }

    // --- 3. AI 出题接口 ---
    @GetMapping("/getExercise")
    public String getExercise(Integer id) {
        User user = userMapper.findById(id);
        if (user == null) return "用户不存在";

        try {
            String prompt = String.format("请为一名%s出一道练习题并附带答案。", user.getRole());
            return callAi(prompt);
        } catch (Exception e) {
            return "出题失败，请检查网络";
        }
    }

     // 提取出来的公共 AI 调用方法（安全升级版）
    private String callAi(String prompt) throws Exception {
        // 使用 ObjectMapper 安全地构造 JSON，防止长文本里的换行符和引号破坏格式！
        ObjectMapper mapper = new ObjectMapper();

        java.util.Map<String, Object> message = new java.util.HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", java.util.Arrays.asList(message));

        // 这样打包出来的 JSON 绝对标准、安全
        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 🌟 新增：把 AI 的真实反应打印在 IDEA 的控制台里，以后再报错我们就能一眼看穿！
        System.out.println("【大模型状态码】：" + response.statusCode());
        System.out.println("【大模型返回内容】：" + response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("大模型接口报错！状态码：" + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }


    // --- 4. 文件上传接口 ---
    @PostMapping("/uploadNote")
    public String uploadNote(Integer userId, org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "上传失败：没有检测到文件！";
        }

        try {
            String dirPath = uploadDir;
            java.io.File dir = new java.io.File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String newFileName = System.currentTimeMillis() + "_" + originalName;
            String filePath = dirPath + newFileName;

            file.transferTo(new java.io.File(filePath));

            com.example.smart_study.entity.Note note = new com.example.smart_study.entity.Note();
            note.setUserId(userId);
            note.setFileName(originalName);
            note.setFilePath(filePath);
            noteMapper.insert(note);
            // 重点：这里不再只返回一串文字，而是把刚刚拿到的 ID 传回去
            // 格式定义为 SUCCESS:ID
            return "SUCCESS:" + note.getId();

        } catch (Exception e) {
            return "FAIL:上传失败，系统错误：" + e.getMessage();
        }

    }

    // --- 5. 新增加的：根据笔记生成测验 ---
    // --- 5. 升级版：动态渲染答题卡专用的 AI 出题接口 ---
    @GetMapping("/generateNoteQuiz")
    public String generateNoteQuiz(Integer noteId) {
        try {
            com.example.smart_study.entity.Note note = noteMapper.getById(noteId);
            if (note == null) {
                return "❌ 错误：找不到这份笔记！";
            }

            String text = com.example.smart_study.util.TextExtractor.extractText(note.getFilePath());

            if (text.length() < 50) {
                return "❌ 笔记内容过少（当前字数：" + text.length() + "字，不足50字），无法生成高质量测验。";
            }

            // 因为要出多道题，我们可以给 AI 喂多一点文本，比如 2000 字
            String content = text.length() > 2000 ? text.substring(0, 2000) : text;

            // 🌟 核心魔法：强迫大模型返回极其标准的 JSON 数组格式
            String prompt = "你是一个严谨的老师。请根据以下笔记内容，生成 3 道单选题用于随堂测验。\n" +
                    "【强制警告】你必须严格按照以下 JSON 数组的格式返回结果！绝对不能包含任何开头或结尾的问候语，绝对不要使用 ```json 这样的 Markdown 标记，直接输出纯净的 JSON 字符串：\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"question\": \"这里是题目内容？\",\n" +
                    "    \"options\": [\"A. 选项一\", \"B. 选项二\", \"C. 选项三\", \"D. 选项四\"],\n" +
                    "    \"correctAnswer\": \"A\",\n" +
                    "    \"analysis\": \"AI解析：这里写为什么选A的原因...\"\n" +
                    "  }\n" +
                    "]\n" +
                    "笔记内容如下：\n" + content;

            return callAi(prompt);

        } catch (Exception e) {
            return "❌ 系统异常：" + e.getMessage();
        }
    }

    // --- 6. 新增加的：用户注册接口 ---
    @org.springframework.web.bind.annotation.PostMapping("/register")
    public String register(String username, String password, String role) {
        try {
            // 简单校验
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                return "FAIL:用户名或密码不能为空";
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setRole(role == null ? "学生" : role);

            userMapper.register(user);
            return "SUCCESS:注册成功，请登录！";
        } catch (Exception e) {
            return "FAIL:注册失败，用户名可能已存在";
        }
    }
    // --- 7. 基础大厅：获取用户笔记列表 ---
    @GetMapping("/listNotes")
    public List<com.example.smart_study.entity.Note> listNotes(Integer userId) {
        // 直接去数据库查
        List<com.example.smart_study.entity.Note> notes = noteMapper.findByUserId(userId);

        // 打印一下看看查到几个，方便调试
        System.out.println("用户 " + userId + " 的笔记数量：" + notes.size());

        return notes;
    }
    // --- 8. AI 智能摘要接口 ---
    @GetMapping("/generateSummary")
    public String generateSummary(Integer noteId) {
        try {
            // 1. 根据 ID 从数据库查出笔记信息
            com.example.smart_study.entity.Note note = noteMapper.getById(noteId);
            if (note == null) {
                return "❌ 找不到该笔记！";
            }

            // 2. 提取文件里的文字
            String text = com.example.smart_study.util.TextExtractor.extractText(note.getFilePath());
            if (text == null || text.trim().isEmpty()) {
                return "❌ 笔记字数不足或无法读取，AI 无法生成摘要。";
            }

            // 3. 🌟 核心：为摘要功能定制专属 Prompt (提示词)
            String prompt = "你是一个专业的AI学习助手。请将以下笔记内容压缩成核心精华摘要，" +
                    "要求：1. 使用 Markdown 格式；2. 提炼出核心考点和逻辑结构；3. 清晰明了，方便复习。\n\n" +
                    "笔记内容如下：\n" + text;

            // 4. 呼叫 AI 大模型
            return callAi(prompt);

        } catch (Exception e) {
            return "❌ 系统异常：" + e.getMessage();
        }
    }
    // --- 9. 删除笔记接口 ---
    @GetMapping("/deleteNote")
    public String deleteNote(Integer noteId) {
        try {
            // 1. 先查出笔记信息（为了拿到文件路径）
            com.example.smart_study.entity.Note note = noteMapper.getById(noteId);
            if (note == null) {
                return "FAIL:笔记不存在";
            }

            // 2. 删除硬盘上的物理文件
            java.io.File file = new java.io.File(note.getFilePath());
            if (file.exists()) {
                file.delete(); // 删掉硬盘里的文件
            }

            // 3. 删除数据库里的记录（假设你的 noteMapper 有 deleteById 方法）
            // 如果报错，请确保在 NoteMapper 接口里定义了：void deleteById(Integer id);
            noteMapper.deleteById(noteId);

            return "SUCCESS";
        } catch (Exception e) {
            return "FAIL:删除失败 " + e.getMessage();
        }
    }

}

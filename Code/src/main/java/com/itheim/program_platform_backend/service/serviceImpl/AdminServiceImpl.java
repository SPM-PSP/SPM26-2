package com.itheim.program_platform_backend.service.serviceImpl;

import com.itheim.program_platform_backend.domain.dto.AdminAddUserDTO;
import com.itheim.program_platform_backend.domain.dto.AdminProblemUpsertDTO;
import com.itheim.program_platform_backend.domain.dto.AdminSolutionCreateDTO;
import com.itheim.program_platform_backend.domain.dto.AdminSolutionUpdateDTO;
import com.itheim.program_platform_backend.domain.po.Category;
import com.itheim.program_platform_backend.domain.po.Problem;
import com.itheim.program_platform_backend.domain.po.ProblemTestCase;
import com.itheim.program_platform_backend.domain.po.Solution;
import com.itheim.program_platform_backend.domain.po.User;
import com.itheim.program_platform_backend.domain.vo.AdminProblemDetailVO;
import com.itheim.program_platform_backend.domain.vo.AdminProblemListItemVO;
import com.itheim.program_platform_backend.domain.vo.AdminTestCaseAddVO;
import com.itheim.program_platform_backend.domain.vo.AdminTestCaseFileVO;
import com.itheim.program_platform_backend.domain.vo.AdminTestCaseVO;
import com.itheim.program_platform_backend.domain.vo.AdminUserListItemVO;
import com.itheim.program_platform_backend.domain.vo.PageResult;
import com.itheim.program_platform_backend.exception.BusinessException;
import com.itheim.program_platform_backend.mapper.AdminMapper;
import com.itheim.program_platform_backend.service.AdminService;
import com.itheim.program_platform_backend.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${file-storage.test-case-root}")
    private String testCaseRoot;

    @Override
    public PageResult<AdminUserListItemVO> getAdminUserList(int page, int size, String keyword, String sortOrder) {
        ensureAdminRole();
        int offset = (page - 1) * size;
        String order = normalizeSortOrder(sortOrder);
        long total = adminMapper.countAdminUsers(keyword);
        List<User> users = adminMapper.selectAdminUsers(offset, size, keyword, order);
        List<AdminUserListItemVO> list = users.stream().map(user -> {
            AdminUserListItemVO vo = new AdminUserListItemVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());

        long pages = total == 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(total, pages, page, list);
    }

    @Override
    @Transactional
    public void deleteAdminUser(Long userId) {
        ensureAdminRole();
        adminMapper.deleteUserTokenByUserId(userId);
        int rows = adminMapper.deleteAdminUserById(userId);
        if (rows == 0) {
            throw new BusinessException(400, "删除用户失败");
        }
    }

    @Override
    public void addAdminUser(AdminAddUserDTO dto) {
        ensureAdminRole();
        validateAddUser(dto);
        if (adminMapper.selectUserByUsername(dto.getUsername()) != null) {
            throw new BusinessException(400, "该用户名已被注册，请更换");
        }
        if (adminMapper.selectUserByEmail(dto.getEmail()) != null) {
            throw new BusinessException(400, "该邮箱已被注册，请更换");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setRole(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int rows = adminMapper.insertUser(user);
        if (rows == 0) {
            throw new BusinessException(400, "添加管理员失败");
        }
    }

    @Override
    public void addCategory(String name) {
        ensureAdminRole();
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(400, "题目分类名不能为空");
        }
        if (adminMapper.selectCategoryByName(name) != null) {
            throw new BusinessException(400, "该类别已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        int rows = adminMapper.insertCategory(name, now, now);
        if (rows == 0) {
            throw new BusinessException(400, "题目类别添加失败");
        }
    }

    @Override
    public void deleteCategory(Long categoryId) {
        ensureAdminRole();
        if (categoryId == null) {
            throw new BusinessException(400, "categoryId不能为空");
        }
        if (adminMapper.countCategoryUsage(categoryId) > 0) {
            throw new BusinessException(400, "该类别已被题目使用，无法删除");
        }
        int rows = adminMapper.deleteCategoryById(categoryId);
        if (rows == 0) {
            throw new BusinessException(400, "删除题目类别失败");
        }
    }

    @Override
    public PageResult<AdminProblemListItemVO> getProblemList(int page, int size, List<String> problemCategory,
                                                            String difficulty, String keyword) {
        ensureAdminRole();
        int offset = (page - 1) * size;
        long total = adminMapper.countProblemList(difficulty, keyword, problemCategory);
        List<Problem> problems = adminMapper.selectProblemList(offset, size, difficulty, keyword, problemCategory);

        List<AdminProblemListItemVO> list = new ArrayList<>();
        for (Problem problem : problems) {
            AdminProblemListItemVO vo = new AdminProblemListItemVO();
            vo.setProblemId(problem.getId());
            vo.setTitle(problem.getTitle());
            vo.setDifficulty(problem.getDifficulty());
            vo.setAcceptRate(problem.getPassRate());
            vo.setCategoryNames(adminMapper.selectCategoryNamesByProblemId(problem.getId()));
            list.add(vo);
        }

        long pages = total == 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(total, pages, page, list);
    }

    @Override
    public AdminProblemDetailVO getProblemDetail(Long problemId) {
        ensureAdminRole();
        Problem problem = adminMapper.selectProblemById(problemId);
        if (problem == null) {
            throw new BusinessException(404, "题目不存在");
        }

        AdminProblemDetailVO vo = new AdminProblemDetailVO();
        vo.setProblemId(problem.getId());
        vo.setTitle(problem.getTitle());
        vo.setDifficulty(problem.getDifficulty());
        vo.setDescription(problem.getDescription());
        vo.setInputFormat(problem.getInputFormat());
        vo.setOutputFormat(problem.getOutputFormat());
        vo.setSampleInput(problem.getSampleInput());
        vo.setSampleOutput(problem.getSampleOutput());
        vo.setTimeLimit(problem.getTimeLimit());
        vo.setMemoryLimit(problem.getMemoryLimit());
        vo.setAcceptRate(problem.getPassRate());
        vo.setCategoryNames(adminMapper.selectCategoryNamesByProblemId(problemId));

        List<ProblemTestCase> testCases = adminMapper.selectProblemTestCasesByProblemId(problemId);
        List<AdminTestCaseVO> caseVOs = testCases.stream()
                .map(c -> new AdminTestCaseVO(c.getInputUrl(), c.getOutputUrl(), c.getCreateTime()))
                .collect(Collectors.toList());
        vo.setTestCases(caseVOs);
        return vo;
    }

    @Override
    @Transactional
    public void updateProblem(AdminProblemUpsertDTO dto) {
        ensureAdminRole();
        validateProblemUpsert(dto, true);
        Problem existing = adminMapper.selectProblemById(dto.getProblemId());
        if (existing == null) {
            throw new BusinessException(404, "题目不存在");
        }

        List<Category> categories = loadCategories(dto.getCategoryNames());

        Problem problem = new Problem();
        problem.setId(dto.getProblemId());
        problem.setTitle(dto.getTitle());
        problem.setDifficulty(dto.getDifficulty());
        problem.setDescription(dto.getDescription());
        problem.setInputFormat(dto.getInputFormat());
        problem.setOutputFormat(dto.getOutputFormat());
        problem.setSampleInput(dto.getSampleInput());
        problem.setSampleOutput(dto.getSampleOutput());
        problem.setTimeLimit(dto.getTimeLimit());
        problem.setMemoryLimit(dto.getMemoryLimit());
        problem.setUpdateTime(LocalDateTime.now());

        adminMapper.updateProblem(problem);

        adminMapper.deleteProblemCategoriesByProblemId(problem.getId());
        for (Category category : categories) {
            adminMapper.insertProblemCategory(problem.getId(), category.getId(), LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void addProblem(AdminProblemUpsertDTO dto) {
        ensureAdminRole();
        validateProblemUpsert(dto, false);
        List<Category> categories = loadCategories(dto.getCategoryNames());

        Problem problem = new Problem();
        problem.setTitle(dto.getTitle());
        problem.setDifficulty(dto.getDifficulty());
        problem.setDescription(dto.getDescription());
        problem.setInputFormat(dto.getInputFormat());
        problem.setOutputFormat(dto.getOutputFormat());
        problem.setSampleInput(dto.getSampleInput());
        problem.setSampleOutput(dto.getSampleOutput());
        problem.setTimeLimit(dto.getTimeLimit());
        problem.setMemoryLimit(dto.getMemoryLimit());
        problem.setPassRate(BigDecimal.ZERO);
        problem.setCreateTime(LocalDateTime.now());
        problem.setUpdateTime(LocalDateTime.now());

        adminMapper.insertProblem(problem);

        for (Category category : categories) {
            adminMapper.insertProblemCategory(problem.getId(), category.getId(), LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public AdminTestCaseFileVO updateProblemTestCase(Long caseId, MultipartFile input, MultipartFile output) {
        ensureAdminRole();
        ProblemTestCase testCase = adminMapper.selectProblemTestCaseById(caseId);
        if (testCase == null) {
            throw new BusinessException(404, "测试用例不存在");
        }
        validateCaseFiles(input, output);

        // 删除旧文件
        deleteOldCaseFiles(testCase);

        String inputRelativePath = storeCaseFile(testCase.getProblemId(), input, "in");
        String outputRelativePath = storeCaseFile(testCase.getProblemId(), output, "out");

        adminMapper.updateProblemTestCase(caseId, inputRelativePath, outputRelativePath, LocalDateTime.now());
        return buildTestCaseFileVO(inputRelativePath, input, outputRelativePath, output);
    }

    @Override
    @Transactional
    public AdminTestCaseAddVO addProblemTestCase(Long problemId, MultipartFile input, MultipartFile output) {
        ensureAdminRole();
        Problem problem = adminMapper.selectProblemById(problemId);
        if (problem == null) {
            throw new BusinessException(404, "题目不存在");
        }
        validateCaseFiles(input, output);

        String inputRelativePath = storeCaseFile(problemId, input, "in");
        String outputRelativePath = storeCaseFile(problemId, output, "out");
        LocalDateTime now = LocalDateTime.now();

        ProblemTestCase testCase = new ProblemTestCase();
        testCase.setProblemId(problemId);
        testCase.setInputUrl(inputRelativePath);
        testCase.setOutputUrl(outputRelativePath);
        testCase.setCreateTime(now);
        testCase.setUpdateTime(now);
        int rows = adminMapper.insertProblemTestCase(testCase);
        if (rows == 0) {
            throw new BusinessException(400, "新增测试用例失败");
        }

        return new AdminTestCaseAddVO(testCase.getId(), now,
                inputRelativePath, input.getSize(), getFileExtension(input),
                outputRelativePath, output.getSize(), getFileExtension(output));
    }

    @Override
    @Transactional
    public void deleteProblemTestCase(Long caseId) {
        ensureAdminRole();
        // 先查询测试用例信息，获取文件路径
        ProblemTestCase testCase = adminMapper.selectProblemTestCaseById(caseId);
        if (testCase == null) {
            throw new BusinessException(404, "测试用例不存在");
        }
        
        // 删除物理文件
        deleteOldCaseFiles(testCase);
        
        // 删除数据库记录
        int rows = adminMapper.deleteProblemTestCaseById(caseId);
        if (rows == 0) {
            throw new BusinessException(404, "测试用例不存在");
        }
    }

    @Override
    public ResponseEntity<MultiValueMap<String, Object>> downloadTestCase(String inputUrl, String outputUrl) {
        ensureAdminRole();
        Path inputPath = resolveSafePath(inputUrl);
        Path outputPath = resolveSafePath(outputUrl);

        if (!Files.exists(inputPath) || !Files.exists(outputPath)) {
            throw new BusinessException(400, "文件不存在或URL异常");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("input", new FileSystemResource(inputPath));
        body.add("output", new FileSystemResource(outputPath));

        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body);
    }

    @Override
    @Transactional
    public void deleteProblem(Long problemId) {
        ensureAdminRole();
        
        // 先查询该题目的所有测试用例，删除物理文件
        List<ProblemTestCase> testCases = adminMapper.selectProblemTestCasesByProblemId(problemId);
        if (testCases != null && !testCases.isEmpty()) {
            log.info("开始删除题目 {} 的 {} 个测试用例文件", problemId, testCases.size());
            for (ProblemTestCase testCase : testCases) {
                deleteOldCaseFiles(testCase);
            }
        }
        
        // 删除相关数据
        adminMapper.deleteEvaluationsByProblemId(problemId);
        adminMapper.deleteSubmissionsByProblemId(problemId);
        adminMapper.deleteUserProblemsByProblemId(problemId);
        adminMapper.deleteProblemTestCasesByProblemId(problemId);
        adminMapper.deleteProblemCategoriesByProblemId(problemId);
        adminMapper.deleteSolutionsByProblemId(problemId);
        int rows = adminMapper.deleteProblemById(problemId);
        if (rows == 0) {
            throw new BusinessException(404, "题目不存在");
        }
    }

    @Override
    public void updateSolution(AdminSolutionUpdateDTO dto) {
        ensureAdminRole();
        validateSolutionUpdate(dto);
        Solution existing = adminMapper.selectSolutionById(dto.getSolutionId());
        if (existing == null) {
            throw new BusinessException(404, "题解不存在");
        }

        Solution solution = new Solution();
        solution.setId(dto.getSolutionId());
        solution.setTitle(dto.getTitle());
        solution.setContent(dto.getContent());
        solution.setLanguage(dto.getLanguage());
        solution.setCode(dto.getCode());
        solution.setUpdateTime(LocalDateTime.now());
        adminMapper.updateSolution(solution);
    }

    @Override
    public void addSolution(AdminSolutionCreateDTO dto) {
        ensureAdminRole();
        validateSolutionCreate(dto);
        Problem problem = adminMapper.selectProblemById(dto.getProblemId());
        if (problem == null) {
            throw new BusinessException(404, "题目不存在");
        }

        Solution solution = new Solution();
        solution.setProblemId(dto.getProblemId());
        solution.setTitle(dto.getTitle());
        solution.setContent(dto.getContent());
        solution.setLanguage(dto.getLanguage());
        solution.setCode(dto.getCode());
        solution.setCreateUserId(UserContext.getCurrentUserId());
        solution.setCreateTime(LocalDateTime.now());
        solution.setUpdateTime(LocalDateTime.now());
        adminMapper.insertSolution(solution);
    }

    @Override
    public void deleteSolution(Long solutionId) {
        ensureAdminRole();
        int rows = adminMapper.deleteSolutionById(solutionId);
        if (rows == 0) {
            throw new BusinessException(404, "删除题目题解失败");
        }
    }

    private void ensureAdminRole() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        Integer role = adminMapper.selectUserRoleById(userId);
        if (role == null || role != 1) {
            throw new BusinessException(403, "权限不足，仅管理员可访问该接口");
        }
    }

    private String normalizeSortOrder(String sortOrder) {
        if (sortOrder == null) {
            return "desc";
        }
        String normalized = sortOrder.toLowerCase(Locale.ROOT);
        return "asc".equals(normalized) ? "asc" : "desc";
    }

    private void validateAddUser(AdminAddUserDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())
                || !StringUtils.hasText(dto.getEmail())) {
            throw new BusinessException(400, "用户信息不能为空");
        }
    }

    private void validateProblemUpsert(AdminProblemUpsertDTO dto, boolean requireId) {
        if (dto == null) {
            throw new BusinessException(400, "题目信息不能为空");
        }
        if (requireId && dto.getProblemId() == null) {
            throw new BusinessException(400, "problemId不能为空");
        }
        if (!StringUtils.hasText(dto.getTitle()) || !StringUtils.hasText(dto.getDifficulty())
                || !StringUtils.hasText(dto.getDescription()) || !StringUtils.hasText(dto.getInputFormat())
                || !StringUtils.hasText(dto.getOutputFormat()) || !StringUtils.hasText(dto.getSampleInput())
                || !StringUtils.hasText(dto.getSampleOutput()) || dto.getTimeLimit() == null
                || dto.getMemoryLimit() == null || dto.getCategoryNames() == null
                || dto.getCategoryNames().isEmpty()) {
            throw new BusinessException(400, "题目信息不完整");
        }
    }

    private List<Category> loadCategories(List<String> categoryNames) {
        List<Category> categories = adminMapper.selectCategoriesByNames(categoryNames);
        if (categories == null || categories.size() != categoryNames.size()) {
            throw new BusinessException(400, "题目分类不存在");
        }
        return categories;
    }

    private void validateCaseFiles(MultipartFile input, MultipartFile output) {
        if (input == null || input.isEmpty()) {
            throw new BusinessException(400, "输入文件不能为空");
        }
        if (output == null || output.isEmpty()) {
            throw new BusinessException(400, "输出文件不能为空");
        }
        String inputExt = getFileExtension(input);
        String outputExt = getFileExtension(output);
        if (!"in".equalsIgnoreCase(inputExt)) {
            throw new BusinessException(400, "输入文件仅支持.in");
        }
        if (!"out".equalsIgnoreCase(outputExt)) {
            throw new BusinessException(400, "输出文件仅支持.out");
        }
    }

    private String storeCaseFile(Long problemId, MultipartFile file, String extension) {
        String fileName = UUID.randomUUID() + "." + extension;
        String relativePath = "problem-" + problemId + "/" + fileName;
        Path targetPath = resolveSafePath(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
        } catch (Exception e) {
            throw new BusinessException(500, "文件保存失败");
        }
        return relativePath;
    }

    private AdminTestCaseFileVO buildTestCaseFileVO(String inputUrl, MultipartFile input,
                                                    String outputUrl, MultipartFile output) {
        return new AdminTestCaseFileVO(inputUrl, input.getSize(), getFileExtension(input),
                outputUrl, output.getSize(), getFileExtension(output));
    }

    private Path resolveSafePath(String relativePath) {
        Path rootPath = Paths.get(testCaseRoot).toAbsolutePath().normalize();
        Path resolved = rootPath.resolve(relativePath).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new BusinessException(400, "文件不存在或URL异常");
        }
        return resolved;
    }

    private String getFileExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(original);
        return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    }

    private void validateSolutionUpdate(AdminSolutionUpdateDTO dto) {
        if (dto == null || dto.getSolutionId() == null || !StringUtils.hasText(dto.getTitle())
                || !StringUtils.hasText(dto.getContent()) || !StringUtils.hasText(dto.getLanguage())
                || !StringUtils.hasText(dto.getCode())) {
            throw new BusinessException(400, "题解信息不完整");
        }
    }

    private void validateSolutionCreate(AdminSolutionCreateDTO dto) {
        if (dto == null || dto.getProblemId() == null || !StringUtils.hasText(dto.getTitle())
                || !StringUtils.hasText(dto.getContent()) || !StringUtils.hasText(dto.getLanguage())
                || !StringUtils.hasText(dto.getCode())) {
            throw new BusinessException(400, "题解信息不完整");
        }
    }

    /**
     * 删除旧的测试用例文件
     */
    private void deleteOldCaseFiles(ProblemTestCase testCase) {
        log.info("开始删除旧测试用例文件, caseId: {}, inputUrl: {}, outputUrl: {}",
                testCase.getId(), testCase.getInputUrl(), testCase.getOutputUrl());
        try {
            if (StringUtils.hasText(testCase.getInputUrl())) {
                Path inputPath = resolveSafePath(testCase.getInputUrl());
                if (Files.exists(inputPath)) {
                    Files.delete(inputPath);
                    log.info("成功删除旧测试输入文件: {}", testCase.getInputUrl());
                } else {
                    log.warn("旧测试输入文件不存在: {}", testCase.getInputUrl());
                }
            }
            if (StringUtils.hasText(testCase.getOutputUrl())) {
                Path outputPath = resolveSafePath(testCase.getOutputUrl());
                if (Files.exists(outputPath)) {
                    Files.delete(outputPath);
                    log.info("成功删除旧测试输出文件: {}", testCase.getOutputUrl());
                } else {
                    log.warn("旧测试输出文件不存在: {}", testCase.getOutputUrl());
                }
            }
        } catch (Exception e) {
            // 记录日志，但不影响主流程
            log.error("删除旧测试文件失败, caseId: {}, 错误: {}", testCase.getId(), e.getMessage(), e);
        }
    }
}




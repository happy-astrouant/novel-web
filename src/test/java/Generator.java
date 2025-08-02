import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonMap;

/**
 * 代码生成器
 *
 */
@Slf4j
public class Generator {

    private static final String USERNAME = "xzy";

    /**
     * 项目信息
     */
    private static final String PROJECT_PATH = System.getProperty("user.dir").replace('\\', '/');
    private static final String JAVA_PATH = "/src/main/java";
    private static final String RESOURCE_PATH = "/src/main/resources";
    private static final String BASE_PACKAGE = "com.xzy.novel";

    /**
     * 数据库信息
     */
    private static final String DATABASE_IP = "localhost";
    private static final String DATABASE_PORT = "3306";
    private static final String DATABASE_NAME = "novel";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = "555371";

    /**
     * 代码生成入口
     */
    public static void main(String[] args) {
        genCode("all"); // 支持表名列表："user,novel,chapter" 或 "all"
    }

    /**
     * 代码生成核心方法
     */
    private static void genCode(String tables) {
        // 1. 构建 JDBC URL（关键参数已增强）
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?" +
                        "useUnicode=true&" +
                        "characterEncoding=utf8&" +
                        "useSSL=false&" +
                        "allowPublicKeyRetrieval=true&" +
                        "serverTimezone=Asia/Shanghai&" +
                        "nullCatalogMeansCurrent=true",  // 👈 关键：确保能读取元数据
                DATABASE_IP, DATABASE_PORT, DATABASE_NAME);

        // 2. 测试数据库连接（提前发现问题）
        if (!testConnection(jdbcUrl)) {
            log.error("❌ 数据库连接失败，请检查 IP、端口、用户名、密码！");
            return;
        }

        // 3. 解析表名
        List<String> tableList = parseTableNames(tables);
        log.info("🎯 即将生成表数量: {}, 表名: {}", tableList.isEmpty() ? "所有表" : tableList.size(), tableList);

        try {
            FastAutoGenerator.create(jdbcUrl, DATABASE_USERNAME, DATABASE_PASSWORD)

                    // 全局配置
                    .globalConfig(builder -> {
                        builder.author(USERNAME)                    // 设置作者
                                .enableSpringdoc()                  // 启用 Swagger 注解（可选）
                                .dateType(DateType.TIME_PACK)     // 使用 Java 8 时间类型
                                .commentDate("yyyy/MM/dd HH:mm:ss")
                                .outputDir(PROJECT_PATH + JAVA_PATH); // 输出到 java 目录
                    })

                    // 包配置
                    .packageConfig(builder -> {
                        builder.parent(BASE_PACKAGE)              // 父包名
                                .entity("dao.entity")             // 实体类包
                                .mapper("dao.mapper")             // Mapper 接口包
                                .service("service")               // Service 接口
                                .serviceImpl("service.impl")      // Service 实现
                                .controller("controller")         // Controller 包（默认 front）
                                // XML 输出路径
                                .pathInfo(singletonMap(OutputFile.xml,
                                        PROJECT_PATH + RESOURCE_PATH + "/mapper"));
                    })

                    // 模板引擎（推荐 Freemarker）
                    // 需要添加依赖：compile 'org.freemarker:freemarker:2.3.32'
                    // .templateEngine(new FreemarkerTemplateEngine())

                    // 模板配置（可选：禁用不需要的）
                    // ✅ 显式指定使用 Freemarker
                    .templateEngine(new FreemarkerTemplateEngine())
                    // 策略配置
                    .strategyConfig(builder -> {
                        builder.addInclude(tableList) // 设置需要生成的表

                                // Entity 策略
                                .entityBuilder()
                                .enableLombok()                       // 启用 Lombok
                                .enableTableFieldAnnotation()         // 字段加 @TableField
                                .formatFileName("%sEntity")           // 命名：XxxEntity
                                .enableFileOverride()                 // 覆盖

                                // Mapper 策略
                                .mapperBuilder()
                                .formatMapperFileName("%sMapper")     // XxxMapper
                                .enableFileOverride()

                                // Service 策略
                                .serviceBuilder().disable()
                                .formatServiceFileName("%sService")
                                .formatServiceImplFileName("%sServiceImpl")
                                .enableFileOverride()

                                // Controller 策略
                                .controllerBuilder().disable()
                                .enableRestStyle()                    // 使用 @RestController
                                .formatFileName("%sController")
                                .enableFileOverride();
                    })

                    // 执行生成
                    .execute();

            log.info("✅ 代码生成成功！路径: {}", PROJECT_PATH);

        } catch (Exception e) {
            log.error("❌ 代码生成失败！", e);
        }
    }

    /**
     * 解析表名：支持 "all" 或 "t1,t2,t3"
     */
    private static List<String> parseTableNames(String tables) {
        if (tables == null || tables.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String trimmed = tables.trim();
        if ("all".equalsIgnoreCase(trimmed)) {
            return Collections.emptyList();
        }
        return Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * 测试数据库连接（提前发现问题）
     */
    private static boolean testConnection(String jdbcUrl) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            DatabaseMetaData meta = conn.getMetaData();
            log.info("✅ 数据库连接成功！Driver: {}, Database: {}", meta.getDriverName(), meta.getURL());

            // 尝试读取一张表的字段（验证元数据可读）
            ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"});
            if (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                ResultSet columns = meta.getColumns(null, null, tableName, "%");
                if (columns.next()) {
                    log.info("✅ 元数据可读：表 '{}' 至少有一个字段 '{}'", tableName, columns.getString("COLUMN_NAME"));
                } else {
                    log.error("❌ 无法读取字段元数据！请检查用户权限（information_schema.COLUMNS）");
                    return false;
                }
            } else {
                log.error("❌ 数据库中没有表！");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("❌ 数据库连接或元数据读取失败！", e);
            return false;
        }
    }
}


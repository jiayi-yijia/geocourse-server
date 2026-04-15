SET @tenant_id = 1001;
SET @teacher_id = 3;
SET @student_id = 4;

DELETE FROM assignment_exam_answer
WHERE tenant_id = @tenant_id
  AND exam_record_id IN (
    SELECT id FROM (
      SELECT id FROM assignment_exam_record
      WHERE tenant_id = @tenant_id AND publish_title LIKE '【测试】%'
    ) t
  );

DELETE FROM assignment_exam_record
WHERE tenant_id = @tenant_id
  AND publish_title LIKE '【测试】%';

DELETE FROM assignment_exam_publish
WHERE tenant_id = @tenant_id
  AND title LIKE '【测试】%';

DELETE FROM assignment_paper_question
WHERE tenant_id = @tenant_id
  AND paper_id IN (
    SELECT id FROM (
      SELECT id FROM assignment_paper
      WHERE tenant_id = @tenant_id AND name LIKE '【测试】%'
    ) t
  );

DELETE FROM assignment_paper
WHERE tenant_id = @tenant_id
  AND name LIKE '【测试】%';

DELETE FROM qb_question_choice
WHERE tenant_id = @tenant_id
  AND question_id IN (
    SELECT id FROM (
      SELECT id FROM qb_question
      WHERE tenant_id = @tenant_id AND title LIKE '【测试】%'
    ) t
  );

DELETE FROM qb_question_answer
WHERE tenant_id = @tenant_id
  AND question_id IN (
    SELECT id FROM (
      SELECT id FROM qb_question
      WHERE tenant_id = @tenant_id AND title LIKE '【测试】%'
    ) t
  );

DELETE FROM qb_question
WHERE tenant_id = @tenant_id
  AND title LIKE '【测试】%';

DELETE FROM qb_category
WHERE tenant_id = @tenant_id
  AND name LIKE '【测试】%';

INSERT INTO qb_category
  (tenant_id, parent_id, name, description, sort_no, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, 0, '【测试】地理题库', '教师端考试模块联调用测试分类', 1, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @cat_root_id = LAST_INSERT_ID();

INSERT INTO qb_category
  (tenant_id, parent_id, name, description, sort_no, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @cat_root_id, '【测试】中国地理', '中国地理测试题', 10, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @cat_cn_id = LAST_INSERT_ID();

INSERT INTO qb_category
  (tenant_id, parent_id, name, description, sort_no, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @cat_root_id, '【测试】世界地理', '世界地理测试题', 20, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @cat_world_id = LAST_INSERT_ID();

INSERT INTO qb_question
  (tenant_id, category_id, title, type, multi_select, difficulty, default_score, analysis, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @cat_cn_id, '【测试】我国面积最大的省级行政区是下列哪一个？', 'CHOICE', 0, 'EASY', 10, '新疆维吾尔自治区面积最大。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @q1_id = LAST_INSERT_ID();

INSERT INTO qb_question_choice
  (tenant_id, question_id, choice_key, choice_text, correct, sort_no, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @q1_id, 'A', '西藏自治区', 0, 0, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @q1_id, 'B', '新疆维吾尔自治区', 1, 1, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @q1_id, 'C', '内蒙古自治区', 0, 2, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @q1_id, 'D', '青海省', 0, 3, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO qb_question_answer
  (tenant_id, question_id, answer_text, grading_rule, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @q1_id, 'B', '单选题按唯一正确选项计分。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO qb_question
  (tenant_id, category_id, title, type, multi_select, difficulty, default_score, analysis, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @cat_cn_id, '【测试】黄河最终注入渤海。', 'JUDGE', 0, 'EASY', 5, '黄河在山东注入渤海。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @q2_id = LAST_INSERT_ID();

INSERT INTO qb_question_answer
  (tenant_id, question_id, answer_text, grading_rule, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @q2_id, 'TRUE', '判断题正确记满分。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO qb_question
  (tenant_id, category_id, title, type, multi_select, difficulty, default_score, analysis, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @cat_cn_id, '【测试】简述季风气候对我国农业生产的影响。', 'TEXT', 0, 'MEDIUM', 15, '可从雨热同期、降水不稳定、区域差异等方面作答。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @q3_id = LAST_INSERT_ID();

INSERT INTO qb_question_answer
  (tenant_id, question_id, answer_text, grading_rule, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @q3_id, '季风气候使我国大部分地区雨热同期，有利于农作物生长；但降水年际变化大，易造成旱涝灾害，需要水利调节。', '主观题关注雨热同期、旱涝波动和农业生产联系。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO qb_question
  (tenant_id, category_id, title, type, multi_select, difficulty, default_score, analysis, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @cat_world_id, '【测试】下列哪条河流流经埃及并孕育了古埃及文明？', 'CHOICE', 0, 'EASY', 10, '答案为尼罗河。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @q4_id = LAST_INSERT_ID();

INSERT INTO qb_question_choice
  (tenant_id, question_id, choice_key, choice_text, correct, sort_no, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @q4_id, 'A', '尼罗河', 1, 0, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @q4_id, 'B', '刚果河', 0, 1, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @q4_id, 'C', '亚马孙河', 0, 2, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @q4_id, 'D', '多瑙河', 0, 3, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO qb_question_answer
  (tenant_id, question_id, answer_text, grading_rule, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @q4_id, 'A', '单选题按唯一正确选项计分。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO qb_question
  (tenant_id, category_id, title, type, multi_select, difficulty, default_score, analysis, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @cat_world_id, '【测试】分析欧洲西部海洋性气候显著的主要原因。', 'TEXT', 0, 'HARD', 20, '可从纬度位置、西风带、北大西洋暖流和地形等角度作答。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @q5_id = LAST_INSERT_ID();

INSERT INTO qb_question_answer
  (tenant_id, question_id, answer_text, grading_rule, status, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @q5_id, '欧洲西部常年受盛行西风影响，濒临大西洋并受北大西洋暖流增温增湿作用，且平原利于海洋水汽深入内陆，因此海洋性气候显著。', '主观题关注西风、洋流、海陆位置和地形。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO assignment_paper
  (tenant_id, name, description, status, total_score, question_count, duration, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, '【测试】中国地理单元测验', '用于教师端组卷、发布和批改联调。', 'PUBLISHED', 30.00, 3, 45, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @paper1_id = LAST_INSERT_ID();

INSERT INTO assignment_paper_question
  (tenant_id, paper_id, question_id, score, sort_no, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @paper1_id, @q1_id, 10.00, 0, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @paper1_id, @q2_id, 5.00, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @paper1_id, @q3_id, 15.00, 2, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO assignment_paper
  (tenant_id, name, description, status, total_score, question_count, duration, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, '【测试】世界地理拓展测验', '用于教师端第二套试卷展示。', 'DRAFT', 30.00, 2, 40, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @paper2_id = LAST_INSERT_ID();

INSERT INTO assignment_paper_question
  (tenant_id, paper_id, question_id, score, sort_no, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @paper2_id, @q4_id, 10.00, 0, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @paper2_id, @q5_id, 20.00, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

INSERT INTO assignment_exam_publish
  (tenant_id, paper_id, title, description, status, start_time, end_time, pass_score, total_score, question_count, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @paper1_id, '【测试】中国地理周测', '已发布的中国地理考试。', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 18.00, 30.00, 3, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @publish1_id = LAST_INSERT_ID();

INSERT INTO assignment_exam_publish
  (tenant_id, paper_id, title, description, status, start_time, end_time, pass_score, total_score, question_count, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @paper2_id, '【测试】世界地理阶段练习', '草稿状态的世界地理考试。', 'DRAFT', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 18.00, 30.00, 2, @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @publish2_id = LAST_INSERT_ID();

INSERT INTO assignment_exam_record
  (tenant_id, publish_id, publish_title, paper_id, student_id, student_name, score, objective_score, subjective_score, answers, start_time, end_time, status, window_switches, grader_id, graded_time, review_comment, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @publish1_id, '【测试】中国地理周测', @paper1_id, @student_id, '李晓雨', 15.00, 15.00, NULL, '待教师批改主观题。', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 90 MINUTE), 'SUBMITTED', 1, NULL, NULL, NULL, @student_id, NOW(), @student_id, NOW(), 0);
SET @record1_id = LAST_INSERT_ID();

INSERT INTO assignment_exam_answer
  (tenant_id, exam_record_id, question_id, question_type, question_title, standard_answer, user_answer, max_score, score, correct_flag, ai_comment, teacher_comment, reviewed, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @record1_id, @q1_id, 'CHOICE', '【测试】我国面积最大的省级行政区是下列哪一个？', 'B', 'B', 10.00, 10.00, 1, '客观题自动判分正确。', NULL, 1, @student_id, NOW(), @student_id, NOW(), 0),
  (@tenant_id, @record1_id, @q2_id, 'JUDGE', '【测试】黄河最终注入渤海。', 'TRUE', 'TRUE', 5.00, 5.00, 1, '客观题自动判分正确。', NULL, 1, @student_id, NOW(), @student_id, NOW(), 0),
  (@tenant_id, @record1_id, @q3_id, 'TEXT', '【测试】简述季风气候对我国农业生产的影响。', '季风气候使我国大部分地区雨热同期，有利于农作物生长；但降水年际变化大，易造成旱涝灾害，需要水利调节。', '季风带来充足热量和降水，利于种植业发展，但雨量不稳定也容易造成旱涝。', 15.00, NULL, NULL, '主观题待教师批改。', NULL, 0, @student_id, NOW(), @student_id, NOW(), 0);

INSERT INTO assignment_exam_record
  (tenant_id, publish_id, publish_title, paper_id, student_id, student_name, score, objective_score, subjective_score, answers, start_time, end_time, status, window_switches, grader_id, graded_time, review_comment, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @publish1_id, '【测试】中国地理周测', @paper1_id, NULL, '王小川', 17.00, 5.00, 12.00, '已完成教师批改。', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 'REVIEWED', 0, @teacher_id, DATE_SUB(NOW(), INTERVAL 2 HOUR), '主观题要点较完整，表达还可更准确。', @teacher_id, NOW(), @teacher_id, NOW(), 0);
SET @record2_id = LAST_INSERT_ID();

INSERT INTO assignment_exam_answer
  (tenant_id, exam_record_id, question_id, question_type, question_title, standard_answer, user_answer, max_score, score, correct_flag, ai_comment, teacher_comment, reviewed, create_by, create_time, update_by, update_time, deleted)
VALUES
  (@tenant_id, @record2_id, @q1_id, 'CHOICE', '【测试】我国面积最大的省级行政区是下列哪一个？', 'B', 'A', 10.00, 0.00, 0, '客观题自动判分错误。', '基础概念掌握不准确。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @record2_id, @q2_id, 'JUDGE', '【测试】黄河最终注入渤海。', 'TRUE', 'TRUE', 5.00, 5.00, 1, '客观题自动判分正确。', NULL, 1, @teacher_id, NOW(), @teacher_id, NOW(), 0),
  (@tenant_id, @record2_id, @q3_id, 'TEXT', '【测试】简述季风气候对我国农业生产的影响。', '季风气候使我国大部分地区雨热同期，有利于农作物生长；但降水年际变化大，易造成旱涝灾害，需要水利调节。', '雨热同期利于农业，但夏季风不稳定会产生旱涝，对农业有双重影响。', 15.00, 12.00, 2, '主观题已给出较完整要点。', '回答到了雨热同期和旱涝风险，得分较高。', 1, @teacher_id, NOW(), @teacher_id, NOW(), 0);

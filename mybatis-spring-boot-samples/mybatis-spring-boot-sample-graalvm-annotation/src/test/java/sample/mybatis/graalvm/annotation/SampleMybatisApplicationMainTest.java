/*
 *    Copyright 2015-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package sample.mybatis.graalvm.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import extensions.annotation.CaptureSystemOutput;
import extensions.annotation.CaptureSystemOutput.OutputCapture;

/**
 * @author Kazuki Shimizu
 */
@CaptureSystemOutput
class SampleMybatisApplicationMainTest {

  @Test
  void test(OutputCapture outputCapture) {
    SampleAnnotationApplication.main(new String[] {});
    String output = outputCapture.toString();
    assertThat(output).contains("1,San Francisco,CA,US");
  }

}

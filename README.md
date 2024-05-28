# apt  annotation process tools
是一款类似于 lombok 的编译时注解框架。

主要提供一些，且自己会用到的常见工具。

编译时注解**性能无任何损失**，一个注解搞定一切，无三方依赖。

实现原理：编译时注解 + 编译原理 AST + (JCTree)

## 创作目的

- 提供常用的功能，便于日常开发使用。

- lombok 的源码基本不可读，应该是加密处理了。

- 为其他注解相关框架提升性能提供基础，后期考虑替换为编译时注解。
## 调试
1，执行MAVEN命令 mvnDebug clean install
2，设置idea远程调试:Remote JVM Debug  端口设置为8000
3，在需要debug的方法上打点调试

## 参考文献
https://blog.csdn.net/u014454538/article/details/122849426
https://blog.csdn.net/duzm200542901104/article/details/126955491
---
layout: default
---

<script type="text/x-mathjax-config">
    MathJax.Hub.Config({
        tex2jax: {
          skipTags: ["script","noscript","style","textarea","pre"],
          inlineMath: [ ['$','$'], ['\(', '\)'] ],
          displayMath: [ ['$$','$$'] ]
        },
        messageStyle: "none"
    });
</script>
<script type="text/javascript" async
  src="https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-MML-AM_CHTML">
</script>

# 流媒体实验三：带宽聚合和分享

2016310592 卢嘉铭

带宽聚合与分享

# 理论计算

## 任务分配问题

有$N$个视频分段，每个视频分段长度为$T$，有$M$个设备，每个设备的下载速率为$r_i, i=1,...,M$。
其中$r_0 > r_1 > ... > r_M$。
视频分段的码率为$r_v$。考虑

$$r_v = \sum_{i=1}^{M}{r_i}$$

证明或证否：存在某种分配算法，使得经过一定延迟之后，视频能够无卡顿播放。可考虑$N = \infty$。

若上述假设成立，找到上述算法，并得出获得最小延迟的分配方案。
若假设不成立，找到使得卡顿时间最小的分配方案。

## 证明

结论：当$r_i \in \mathbb{Q}$时，可以构造一个任务分配算法，该算法可以保证一定延迟之后视频无卡顿播放；
当$\exists r_i$满足$r_i\notin \mathbb{Q}, r_i \in \mathbb{R}$，该问题无解。

构造方法如下，
对所有的$r_i \in \mathbb{Q}$，找到一个最小的$K \in \mathbb{N}$ 将他们化为整数$K r_i$。
我们定义一个下载时间段

$$ T_{peroid} = T K \sum_{i = 1}^{M}{r_i} = T K r_v $$

在该时间段中，每个设备下载$K r_i$个任务，下载完的任务总数为$K r_v$，播放时间为$K r_v T$。
对每个设备，下载时间

$$T_{download} = K r_i \frac{T r_v}{r_i} = K T r_v $$

与播放时间相同，所以通过该方法，可以保证在一定延迟之后，视频一定连续无卡顿播放。
经过上述方法的启发，我们可以证明，视频无卡顿播放的充要条件是存在一个时间段下载的视频分段总量与播放视频分段总量相等。
利用反证法，可以知道上述方法求出的时间段一定是最小的时间段，否则与$K$最小矛盾。

上述构造方法没有指定具体的任务分配，我们可以找到一种具体的任务分配使得延迟最小。
考虑上述的一个阶段时间，假定视频从时刻0开始播放，任务从时刻0开始下载。
假定最小延迟为$T_{delay}$考虑第$i$个视频分段的下载完成时间$t_{i, f}$以及该分段对应的播放时间$t_{i, p}$，

$$ T_{delay} = \min_{p \in all}{\max_{i = 1}^{M}{(t_{i, f} - t_{i, p})}}$$

式中$p$代表任务分配方案。
该问题满足子系统最优，可以使用贪心法得到最优解。

考虑到在阶段结束时共有$M$个分段下载完成，这些分段中序号最小的分段延迟最大，可得

$$ T_{delay} \geq MT $$

通过该式可以对最小延迟进行一个估计

# 任务分配算法

原系统中已经实现的任务分配策略是TaskAssigner Simple和TaskAssigner Paper两个类。
下面描述了这两种方法以及他们的缺陷。
同时，基于理论计算设计了新的任务分配策略并针对两种方法的缺陷进行了改进。

## TaskAssigner Simple

轮询每个设备，找到第一个可以下载的视频分段，分配之。
通过简单的加法选择对应的码率，在设备加入后，立即更新对应的码率。
该方法的缺陷如下

1. 简单的加法选择码率并立即更新码率，可能导致缓冲不及时，在切换时发生卡顿。
2. 未考虑每个设备的下载能力针对性地分配任务。下载速度较慢的设备仍可能分配一个较近的任务，导致等待其下载发生卡顿。
3. 网络环境变化可能发生阻塞，一旦某个设备网络突然断开，则视频观看发生阻塞，没有鲁棒性。

## TaskAssigner Paper

其他部分相同
对于原方法的第一条缺陷，这个算法提出了一下改进。
计算下载的码率时使用如下函数，防止波动。
缓慢上涨，迅速下降的方法减少因为码率切换带来卡顿的可能性。
\begin{eqnarray}
& tmp = e^{p (T_{buffer} - 30)} \\\\
& f = \frac{tmp}{1 - tmp}
\end{eqnarray}

但该方法对第二、三条缺陷没有进行改进，仍然具有相同问题。


## 最优任务分配算法

上述两种方法均在于局域网环境测试，通过延时限速。
而事实上，在复杂网络环境下，鲁棒性是很重要的问题。
我们先考虑原方法的缺陷三，
原有的上述两种任务分配算法，任务分配只发生在下列情况。

1. 开始播放时
2. 设备加入时
3. 下载任务完成时

在上述三种情况发生时，任务管理器查找可行的片段分配给对应的设备。
这种分配方式存在明显缺陷，假如任务分配完成后，网络情况发生变化，例如从设备网络断开但并未退出，
很可能导致该下载任务阻塞，用户无法继续观看视频。
同时，该方法是基于设备响应的，并不能通过分析设备的网速全局地控制任务的分配。这也是缺陷二的原因。

为了上述问题，我们放弃采用基于设备响应的任务分配，而使用心跳机制，每隔一段时间对当前网络环境进行检测。若网络环境变化较大，可能需要对任务进行重新分配，对不合适的分配进行纠正。

基于上述的心跳机制，我们基于视频分段对任务进行分配，通过维护一个设备网络信息的表，查询当前网络环境并选择最优解。

通过理论计算部分的分析，我们可以对应地设计一种最优任务分配算法来选择最优解。
通过理论分析，在设备连接数量$M$及网络环境不变的情况下，我们经过$MT$的延迟之后开始播放，对每个视频分段，我们将这个任务分配给预期下载完成时间离当前最近的设备，根据理论分析，该种分配方法一定能够获得最小的延迟。

在网络环境发生变化的情况下，我们需要根据不同情况分别处理

1. 当新设备加入时，不改变当前已分配的任务。计算一个新的码率，从当前播放片段后的第$M$个片段开始，更新视频码率。
2. 当设备离开时，我们规定设备离开前必须完成当前下载任务。不该变当前已分配的任务，对下一个分配的任务，更新视频码率。
3. 当某个设备的网速突然变得极低。类似于该设备强制断开连接，我们将该设备移除当前任务分配列表，对所有任务进行重新分配。
4. 当某个设备的网速突然提高，近似于新设备加入，我们计算一个新的码率，从当前播放片段后的第$M$个片段开始，更新视频码率。
5. 当某个设备的网速突然降低，但仍可继续下载，我们对下一个分配的任务开始，立即降低码率。

# Demo
Text can be **bold**, _italic_, or ~~strikethrough~~.

There should be whitespace between paragraphs.

There should be whitespace between paragraphs. We recommend including a README, or a file with information about your project.

# [](#header-1)Header 1

This is a normal paragraph following a header. GitHub is a code hosting platform for version control and collaboration. It lets you and others work together on projects from anywhere.

## [](#header-2)Header 2

> This is a blockquote following a header.
>
> When something is important enough, you do it even if the odds are not in your favor.

### [](#header-3)Header 3

```js
// Javascript code with syntax highlighting.
var fun = function lang(l) {
  dateformat.i18n = require('./lang/' + l)
  return true;
}
```

```ruby
# Ruby code with syntax highlighting
GitHubPages::Dependencies.gems.each do |gem, version|
  s.add_dependency(gem, "= #{version}")
end
```

#### [](#header-4)Header 4

*   This is an unordered list following a header.
*   This is an unordered list following a header.
*   This is an unordered list following a header.

##### [](#header-5)Header 5

1.  This is an ordered list following a header.
2.  This is an ordered list following a header.
3.  This is an ordered list following a header.

###### [](#header-6)Header 6

| head1        | head two          | three |
|:-------------|:------------------|:------|
| ok           | good swedish fish | nice  |
| out of stock | good and plenty   | nice  |
| ok           | good `oreos`      | hmm   |
| ok           | good `zoute` drop | yumm  |

### There's a horizontal rule below this.

* * *

### Here is an unordered list:

*   Item foo
*   Item bar
*   Item baz
*   Item zip

### And an ordered list:

1.  Item one
1.  Item two
1.  Item three
1.  Item four

### And a nested list:

- level 1 item
  - level 2 item
  - level 2 item
    - level 3 item
    - level 3 item
- level 1 item
  - level 2 item
  - level 2 item
  - level 2 item
- level 1 item
  - level 2 item
  - level 2 item
- level 1 item

### Small image

![](https://assets-cdn.github.com/images/icons/emoji/octocat.png)

### Large image

![](https://guides.github.com/activities/hello-world/branching.png)


### Definition lists can be used with HTML syntax.

<dl>
<dt>Name</dt>
<dd>Godzilla</dd>
<dt>Born</dt>
<dd>1952</dd>
<dt>Birthplace</dt>
<dd>Japan</dd>
<dt>Color</dt>
<dd>Green</dd>
</dl>

```
Long, single-line code blocks should not wrap. They should horizontally scroll if they are too long. This line should be long enough to demonstrate this.
```

```
The final element.
```

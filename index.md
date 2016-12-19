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

结论：当$r_i \in Q$时，可以构造一个任务分配算法，该算法可以保证一定延迟之后视频无卡顿播放；
当$\exists r_i$满足$r_i\notin Q, r_i \in R$，该问题无解。

构造方法如下，
对所有的$r_i \in Q$，找到一个最小的$k \in N$ 将他们化为整数$K r_i$。
我们定义一个下载时间段

$$ T_{peroid} = T K \sum_{i = 1}^{M}{r_i} = T K r_v $$

在该时间段中，每个设备下载$K r_i$个任务，下载完的任务总数为$K r_v$，播放时间为$K r_v T$。
对每个设备，下载时间

$$T_{download} = K r_i \frac{T r_v}{r_i} = K T r_v $$

与播放时间相同，所以通过该方法，可以保证在一定延迟之后，视频一定连续无卡顿播放。
经过上述方法的启发，我们可以证明，视频无卡顿播放的充要条件是存在一个时间段下载总量与播放总量相等。
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

## 算法

# 实现

## Demo


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

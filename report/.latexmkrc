# 强制使用 XeLaTeX 编译（本模板需 XeLaTeX 支持中文）
$pdf_mode = 5;
$xelatex = 'xelatex -synctex=1 -interaction=nonstopmode -file-line-error %O %S';

# 无参考文献时自动跳过 bibtex，避免 "no \citation commands" 报错
$bibtex = 'bibtex %O %B';
$bibtex_use = 1;

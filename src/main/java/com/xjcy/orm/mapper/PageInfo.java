package com.xjcy.orm.mapper;

public class PageInfo
{
	private int pageNum;
	private int pageSize;
	private int startRow;
	private int endRow;
	private long total;
	private int pages;

	public PageInfo(int pageNum, int pageSize)
	{
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.startRow = pageNum > 0 ? (pageNum - 1) * pageSize : 0;
		this.endRow = pageNum * pageSize;
	}

	public int getPages()
	{
		return pages;
	}

	public void setPages(int pages)
	{
		this.pages = pages;
	}

	public int getEndRow()
	{
		return endRow;
	}

	public void setEndRow(int endRow)
	{
		this.endRow = endRow;
	}

	public int getPageNum()
	{
		return pageNum;
	}

	public void setPageNum(int pageNum)
	{
		this.pageNum = pageNum;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	public int getStartRow()
	{
		return startRow;
	}

	public void setStartRow(int startRow)
	{
		this.startRow = startRow;
	}

	public long getTotal()
	{
		return total;
	}

	public void setTotal(Long total2) {
		this.total = total2;
		this.setPages((int) Math.ceil(total2 / Double.parseDouble(pageSize + "")));
	}

	@Override
	public String toString()
	{
		return "Page{" + "pageNum=" + pageNum + ", pageSize=" + pageSize + ", startRow=" + startRow + ", endRow="
				+ endRow + ", total=" + total + ", pages=" + pages + '}';
	}
}

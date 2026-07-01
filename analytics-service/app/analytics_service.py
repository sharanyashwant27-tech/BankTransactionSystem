from datetime import date
from io import BytesIO, StringIO
from typing import Any

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import pandas as pd
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models import Transaction, User


def _transactions_dataframe(db: Session) -> pd.DataFrame:
    stmt = (
        select(Transaction, User.username)
        .join(User, Transaction.user_id == User.id, isouter=True)
        .order_by(Transaction.transaction_date.desc())
    )
    rows = db.execute(stmt).all()

    records = [
        {
            "id": tx.id,
            "user_id": tx.user_id,
            "username": username,
            "description": tx.description,
            "amount": float(tx.amount) if tx.amount is not None else 0.0,
            "transaction_date": tx.transaction_date,
        }
        for tx, username in rows
    ]

    df = pd.DataFrame(records)
    if df.empty:
        return df

    df["transaction_date"] = pd.to_datetime(df["transaction_date"])
    df["year"] = df["transaction_date"].dt.year
    df["month"] = df["transaction_date"].dt.month
    df["month_name"] = df["transaction_date"].dt.strftime("%B")
    return df


def monthly_transaction_report(db: Session, year: int, month: int) -> dict[str, Any]:
    df = _transactions_dataframe(db)
    if df.empty:
        return {
            "year": year,
            "month": month,
            "total_transactions": 0,
            "total_amount": 0.0,
            "average_amount": 0.0,
            "transactions": [],
        }

    monthly = df[(df["year"] == year) & (df["month"] == month)]
    transactions = [
        {
            "id": int(row["id"]),
            "user_id": int(row["user_id"]) if pd.notna(row["user_id"]) else None,
            "username": row["username"],
            "description": row["description"],
            "amount": round(float(row["amount"]), 2),
            "transaction_date": row["transaction_date"].strftime("%Y-%m-%d"),
        }
        for _, row in monthly.iterrows()
    ]

    return {
        "year": year,
        "month": month,
        "month_name": date(year, month, 1).strftime("%B"),
        "total_transactions": len(monthly),
        "total_amount": round(float(monthly["amount"].sum()), 2) if not monthly.empty else 0.0,
        "average_amount": round(float(monthly["amount"].mean()), 2) if not monthly.empty else 0.0,
        "transactions": transactions,
    }


def spending_analysis(db: Session, user_id: int | None = None) -> dict[str, Any]:
    df = _transactions_dataframe(db)
    if df.empty:
        return {
            "total_spending": 0.0,
            "transaction_count": 0,
            "average_transaction": 0.0,
            "highest_transaction": 0.0,
            "lowest_transaction": 0.0,
            "by_user": [],
            "top_descriptions": [],
        }

    if user_id is not None:
        df = df[df["user_id"] == user_id]

    by_user = (
        df.groupby(["user_id", "username"], dropna=False)["amount"]
        .agg(["count", "sum", "mean"])
        .reset_index()
        .sort_values("sum", ascending=False)
    )

    top_descriptions = (
        df.groupby("description")["amount"]
        .agg(["count", "sum"])
        .reset_index()
        .sort_values("sum", ascending=False)
        .head(5)
    )

    return {
        "user_id": user_id,
        "total_spending": round(float(df["amount"].sum()), 2),
        "transaction_count": int(len(df)),
        "average_transaction": round(float(df["amount"].mean()), 2),
        "highest_transaction": round(float(df["amount"].max()), 2),
        "lowest_transaction": round(float(df["amount"].min()), 2),
        "by_user": [
            {
                "user_id": int(row["user_id"]) if pd.notna(row["user_id"]) else None,
                "username": row["username"],
                "transaction_count": int(row["count"]),
                "total_spending": round(float(row["sum"]), 2),
                "average_spending": round(float(row["mean"]), 2),
            }
            for _, row in by_user.iterrows()
        ],
        "top_descriptions": [
            {
                "description": row["description"] or "N/A",
                "transaction_count": int(row["count"]),
                "total_spending": round(float(row["sum"]), 2),
            }
            for _, row in top_descriptions.iterrows()
        ],
    }


def monthly_spending_chart(db: Session, year: int) -> BytesIO:
    df = _transactions_dataframe(db)
    buffer = BytesIO()

    if df.empty:
        plt.figure(figsize=(8, 5))
        plt.title(f"Monthly Spending - {year}")
        plt.text(0.5, 0.5, "No transaction data", ha="center", va="center")
        plt.axis("off")
        plt.savefig(buffer, format="png", bbox_inches="tight")
        plt.close()
        buffer.seek(0)
        return buffer

    yearly = df[df["year"] == year]
    monthly_totals = (
        yearly.groupby("month")["amount"]
        .sum()
        .reindex(range(1, 13), fill_value=0)
    )

    plt.figure(figsize=(10, 5))
    plt.bar(monthly_totals.index, monthly_totals.values, color="#0066cc")
    plt.xlabel("Month")
    plt.ylabel("Total Amount")
    plt.title(f"Monthly Spending - {year}")
    plt.xticks(range(1, 13), ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"])
    plt.tight_layout()
    plt.savefig(buffer, format="png")
    plt.close()
    buffer.seek(0)
    return buffer


def spending_by_user_chart(db: Session) -> BytesIO:
    df = _transactions_dataframe(db)
    buffer = BytesIO()

    if df.empty:
        plt.figure(figsize=(8, 5))
        plt.title("Spending by User")
        plt.text(0.5, 0.5, "No transaction data", ha="center", va="center")
        plt.axis("off")
        plt.savefig(buffer, format="png", bbox_inches="tight")
        plt.close()
        buffer.seek(0)
        return buffer

    user_totals = (
        df.groupby("username")["amount"]
        .sum()
        .sort_values(ascending=False)
    )

    plt.figure(figsize=(10, 5))
    plt.bar(user_totals.index.astype(str), user_totals.values, color="#0066cc")
    plt.xlabel("User")
    plt.ylabel("Total Spending")
    plt.title("Spending by User")
    plt.xticks(rotation=30, ha="right")
    plt.tight_layout()
    plt.savefig(buffer, format="png")
    plt.close()
    buffer.seek(0)
    return buffer


def export_monthly_csv(db: Session, year: int, month: int) -> str:
    df = _transactions_dataframe(db)
    if df.empty:
        return "id,user_id,username,description,amount,transaction_date\n"

    monthly = df[(df["year"] == year) & (df["month"] == month)]
    export_df = monthly[["id", "user_id", "username", "description", "amount", "transaction_date"]].copy()
    export_df["transaction_date"] = export_df["transaction_date"].dt.strftime("%Y-%m-%d")

    output = StringIO()
    export_df.to_csv(output, index=False)
    return output.getvalue()
